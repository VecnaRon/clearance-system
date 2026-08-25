// services/clearanceService.js
import { pool } from "../config/db.js";
import PDFDocument from "pdfkit";

async function logAudit(actorEmail, action, details) {
  await pool.query(
    "INSERT INTO audit_logs (action, actor_email, created_at, details) VALUES (?, ?, NOW(6), ?)",
    [action, actorEmail, details]
  );
}

export async function startClearance(email) {
  const [[student]] = await pool.query("SELECT * FROM users WHERE email = ?", [email]);
  if (!student) {
    throw Object.assign(new Error("User not found"), { status: 404 });
  }

  const [[existing]] = await pool.query(
    "SELECT * FROM clearances WHERE student_id = ? ORDER BY created_at DESC LIMIT 1",
    [student.id]
  );

  if (existing && (existing.status === "pending" || existing.status === "awaiting_final")) {
    throw Object.assign(new Error("You already have an active clearance request"), { status: 400 });
  }

  const connection = await pool.getConnection();
  try {
    await connection.beginTransaction();

    const [result] = await connection.query(
      "INSERT INTO clearances (status, student_id, created_at, updated_at) VALUES ('pending', ?, NOW(6), NOW(6))",
      [student.id]
    );
    const clearanceId = result.insertId;

    // Matches Spring's departmentRepository.findAll() — ALL departments,
    // no is_active filter (unlike the old Node code).
    const [departments] = await connection.query("SELECT * FROM departments");

    if (departments.length === 0) {
      throw Object.assign(
        new Error("No departments found. Please ask admin to add departments first."),
        { status: 400 }
      );
    }

    const stepValues = departments.map((d) => [clearanceId, d.id, "pending"]);
    await connection.query(
      "INSERT INTO clearance_steps (clearance_id, department_id, status) VALUES ?",
      [stepValues]
    );

    await connection.commit();

    await logAudit(
      student.email,
      "STARTED_CLEARANCE",
      `Clearance #${clearanceId} started by ${student.full_name} (Admission: ${student.admission_number})`
    );

    return clearanceId;
  } catch (err) {
    await connection.rollback();
    throw err;
  } finally {
    connection.release();
  }
}

export async function getLatestForStudent(email) {
  const [[student]] = await pool.query("SELECT * FROM users WHERE email = ?", [email]);
  if (!student) {
    throw Object.assign(new Error("User not found"), { status: 404 });
  }

  const [[clearance]] = await pool.query(
    "SELECT * FROM clearances WHERE student_id = ? ORDER BY created_at DESC LIMIT 1",
    [student.id]
  );

  if (!clearance) {
    // Matches Spring: 200 OK with a message, NOT a 404
    return { message: "No clearance found" };
  }

  const [steps] = await pool.query(
    `SELECT cs.*, d.name AS department_name
     FROM clearance_steps cs
     LEFT JOIN departments d ON d.id = cs.department_id
     WHERE cs.clearance_id = ?`,
    [clearance.id]
  );

  const departments = steps.map((s) => ({
    id: s.id,
    department_name: s.department_name || "Unknown",
    status: s.status,
    remarks: s.comment || "",
    has_dues: false,
    dues_amount: 0,
  }));

  return {
    id: clearance.id,
    admission_number: student.admission_number || "",
    status: clearance.status,
    submitted_at: clearance.created_at,
    departments,
  };
}

export async function approveFinal(clearanceId, approverEmail) {
  const [[clearance]] = await pool.query("SELECT * FROM clearances WHERE id = ?", [clearanceId]);
  if (!clearance) {
    throw Object.assign(new Error("Clearance not found"), { status: 404 });
  }

  const [steps] = await pool.query("SELECT * FROM clearance_steps WHERE clearance_id = ?", [clearanceId]);
  const allApproved = steps.every((s) => s.status === "approved"); // matches Java allMatch semantics

  if (!allApproved) {
    throw Object.assign(new Error("Not all departments have approved yet"), { status: 400 });
  }

  await pool.query("UPDATE clearances SET status = 'approved', updated_at = NOW(6) WHERE id = ?", [clearanceId]);

  await logAudit(approverEmail || "admin", "FINAL_APPROVED", `Final approval granted for clearance #${clearanceId}`);

  return { message: "Final approval granted successfully" };
}



export async function generateCertificate(clearanceId) {
  const [[clearance]] = await pool.query("SELECT * FROM clearances WHERE id = ?", [clearanceId]);
  if (!clearance) {
    throw Object.assign(new Error("Clearance not found"), { status: 404 });
  }
  if (clearance.status !== "approved") {
    throw Object.assign(
      new Error("Certificate not available. Clearance is not fully approved."),
      { status: 403 }
    );
  }

  const [steps] = await pool.query("SELECT * FROM clearance_steps WHERE clearance_id = ?", [clearanceId]);
  const allStillApproved = steps.every((s) => s.status === "approved");
  if (!allStillApproved) {
    throw Object.assign(
      new Error("Certificate not available. Some departments have not approved."),
      { status: 403 }
    );
  }

  const [[student]] = await pool.query("SELECT * FROM users WHERE id = ?", [clearance.student_id]);
  if (!student) {
    throw Object.assign(new Error("Student not found"), { status: 404 });
  }


  // ── Add this helper just above buildCertificatePdf (or anywhere in module scope) ──
  function drawGradCapIcon(doc, cx, cy, scale, color) {
    doc.save();
    // cap top (diamond)
    doc.moveTo(cx, cy - 10 * scale)
      .lineTo(cx + 18 * scale, cy)
      .lineTo(cx, cy + 10 * scale)
      .lineTo(cx - 18 * scale, cy)
      .closePath()
      .fill(color);
    // band
    doc.rect(cx - 8 * scale, cy + 6 * scale, 16 * scale, 6 * scale).fill(color);
    // tassel
    doc.moveTo(cx + 18 * scale, cy + 2 * scale)
      .lineTo(cx + 18 * scale, cy + 16 * scale)
      .lineWidth(1.2).strokeColor(color).stroke();
    doc.circle(cx + 18 * scale, cy + 18 * scale, 1.8 * scale).fill(color);
    doc.restore();
  }

  // ── Replace the existing buildCertificatePdf function with this ──
  function buildCertificatePdf(student, clearance) {
    return new Promise((resolve, reject) => {
      const doc = new PDFDocument({ size: "A4", margin: 0 });
      const chunks = [];
      doc.on("data", (chunk) => chunks.push(chunk));
      doc.on("end", () => resolve(Buffer.concat(chunks)));
      doc.on("error", reject);

      const NAVY = "#1e293b";
      const GOLD = "#b45309";
      const SLATE = "#334155";
      const pageWidth = doc.page.width;
      const pageHeight = doc.page.height;

      // Outer decorative border (navy) + inner hairline (gold)
      doc.lineWidth(3).strokeColor(NAVY)
        .rect(14, 14, pageWidth - 28, pageHeight - 28).stroke();
      doc.lineWidth(1).strokeColor(GOLD)
        .rect(20, 20, pageWidth - 40, pageHeight - 40).stroke();

      // Faded watermark emblem, centered in the body
      doc.save();
      doc.opacity(0.05);
      drawGradCapIcon(doc, pageWidth / 2, 460, 9, NAVY);
      doc.opacity(1);
      doc.restore();
      
        // Header band
      doc.rect(14, 14, pageWidth - 28, 110).fill(NAVY);
      drawGradCapIcon(doc, 80, 66, 1.4, "#ffffff");

      doc.fillColor("#ffffff").font("Helvetica-Bold").fontSize(22)
        .text("ClearanceSystem", 110, 55);
      doc.font("Helvetica").fontSize(10).fillColor("#cbd5e1")
        .text("Official School Clearance Certification", 110, 82);

      // NEW — right-aligned info block in the header band
      const headerRightWidth = 220;
      const headerRightX = pageWidth - 24 - headerRightWidth - 20;

      doc.font("Helvetica-Bold").fontSize(9).fillColor("#cbd5e1")
        .text("P.O. Box 1234-00100, Nairobi, Kenya", headerRightX, 55, {
          width: headerRightWidth,
          align: "right",
        });

      doc.font("Helvetica").fontSize(8).fillColor("#94a3b8")
        .text(
          `Generated: ${new Date().toLocaleString("en-KE", {
            day: "numeric",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
          })}`,
          headerRightX, 108, { width: headerRightWidth, align: "right" }
        );

      // Certificate title
      doc.fillColor(NAVY).font("Times-Bold").fontSize(28)
        .text("CERTIFICATE OF CLEARANCE", 0, 170, { align: "center" });

      // Gold divider
      doc.moveTo(pageWidth / 2 - 100, 210).lineTo(pageWidth / 2 + 100, 210)
        .lineWidth(2).strokeColor(GOLD).stroke();

      doc.font("Times-Roman").fontSize(13).fillColor(SLATE)
        .text("This is to certify that", 0, 260, { align: "center" });

      doc.font("Times-BoldItalic").fontSize(26).fillColor(NAVY)
        .text(student.full_name, 0, 285, { align: "center" });

      doc.font("Times-Roman").fontSize(12).fillColor(SLATE)
        .text(`Admission Number: ${student.admission_number || "N/A"}`, 0, 325, { align: "center" });

      doc.font("Times-Roman").fontSize(13).fillColor(SLATE)
        .text(
          "has successfully completed all clearance requirements across every department and is hereby officially cleared by the school administration.",
          80, 360, { align: "center", width: pageWidth - 160 }
        );

      // Reference / date box
      const boxY = 450;
      doc.roundedRect(80, boxY, pageWidth - 160, 60, 6)
        .lineWidth(1).strokeColor("#cbd5e1").stroke();

      doc.font("Helvetica-Bold").fontSize(10).fillColor("#64748b")
        .text("CLEARANCE REFERENCE", 100, boxY + 12);
      doc.font("Helvetica").fontSize(12).fillColor(NAVY)
        .text(`#${clearance.id}`, 100, boxY + 28);

      doc.font("Helvetica-Bold").fontSize(10).fillColor("#64748b")
        .text("DATE ISSUED", pageWidth / 2 + 20, boxY + 12);
      doc.font("Helvetica").fontSize(12).fillColor(NAVY)
        .text(
          new Date().toLocaleDateString("en-KE", { day: "numeric", month: "long", year: "numeric" }),
          pageWidth / 2 + 20, boxY + 28
        );

      // Signature line
      const sigY = 640;
      doc.moveTo(pageWidth - 280, sigY).lineTo(pageWidth - 100, sigY)
        .lineWidth(1).strokeColor(NAVY).stroke();
      doc.font("Helvetica").fontSize(10).fillColor("#475569")
        .text("Authorized Signature", pageWidth - 280, sigY + 6, { width: 180, align: "center" });

      // Gold seal
      doc.circle(140, sigY - 10, 32).lineWidth(2).strokeColor(GOLD).stroke();
      doc.circle(140, sigY - 10, 26).lineWidth(1).strokeColor(GOLD).stroke();
      doc.font("Helvetica-Bold").fontSize(9).fillColor(GOLD)
        .text("OFFICIAL", 110, sigY - 16, { width: 60, align: "center" });
      doc.text("SEAL", 110, sigY - 4, { width: 60, align: "center" });

      // Footer note
      doc.font("Helvetica").fontSize(8).fillColor("#94a3b8")
        .text(
          "This certificate is system-generated and valid without a physical signature.",
          0, pageHeight - 80, { align: "center" }
        );

      doc.end();
    });
  }
  
  const buffer = await buildCertificatePdf(student, clearance);
  const filename = `clearance_certificate_${student.admission_number}.pdf`;

  return { buffer, filename };
}