//departmentService.js
import { pool } from "../config/db.js";

async function logAudit(actorEmail, action, details) {
  await pool.query(
    "INSERT INTO audit_logs (action, actor_email, created_at, details) VALUES (?, ?, NOW(6), ?)",
    [action, actorEmail, details]
  );
}

export async function queueForHod(email) {
  const [[hod]] = await pool.query("SELECT * FROM users WHERE email = ?", [email]);
  if (!hod || !hod.department_id) {
    throw Object.assign(new Error("You are not assigned to a department"), { status: 400 });
  }

  const [steps] = await pool.query("SELECT * FROM clearance_steps WHERE department_id = ?", [hod.department_id]);

  const queue = [];
  for (const step of steps) {
    const [[clearance]] = await pool.query("SELECT * FROM clearances WHERE id = ?", [step.clearance_id]);
    const student = clearance
      ? (await pool.query("SELECT * FROM users WHERE id = ?", [clearance.student_id]))[0][0]
      : null;

    queue.push({
      clearance_step_id: step.id,
      clearanceId: step.clearance_id,
      full_name: student ? student.full_name : "Unknown",
      admission_number: student && student.admission_number ? student.admission_number : "",
      status: step.status,
    });
  }
  return queue;
}

export async function getStepDetail(stepId) {
  const [[step]] = await pool.query("SELECT * FROM clearance_steps WHERE id = ?", [stepId]);
  if (!step) throw Object.assign(new Error("Step not found"), { status: 404 });

  const [[clearance]] = await pool.query("SELECT * FROM clearances WHERE id = ?", [step.clearance_id]);
  const student = clearance
    ? (await pool.query("SELECT * FROM users WHERE id = ?", [clearance.student_id]))[0][0]
    : null;
  const [[dept]] = await pool.query("SELECT * FROM departments WHERE id = ?", [step.department_id]);

  const [records] = await pool.query(
    "SELECT * FROM department_records WHERE student_id = ? AND department_id = ?",
    [student ? student.id : -1, step.department_id]
  );

  const recordList = [];
  for (const r of records) {
    const [[logger]] = await pool.query("SELECT full_name FROM users WHERE id = ?", [r.logged_by]);
    recordList.push({
      id: r.id,
      description: r.description,
      amount: r.amount,
      status: r.status,
      createdAt: r.created_at,
      loggedBy: logger ? logger.full_name : "Unknown",
    });
  }

  return {
    id: step.id,
    clearanceId: step.clearance_id,
    department_name: dept ? dept.name : "Unknown",
    full_name: student ? student.full_name : "Unknown",
    admission_number: student && student.admission_number ? student.admission_number : "",
    status: step.status,
    remarks: step.comment || "",
    has_dues: false,
    dues_amount: 0,
    records: recordList,
  };
}

export async function decideStep(stepId, { status: rawStatus, remarks }, actorEmail) {
  const [[step]] = await pool.query("SELECT * FROM clearance_steps WHERE id = ?", [stepId]);
  if (!step) throw Object.assign(new Error("Step not found"), { status: 404 });

   const newStatus = rawStatus === "cleared" ? "approved" : rawStatus === "rejected" ? "rejected" : "pending";

  // NEW: block approval if the student has any unresolved department_records in this department
  if (newStatus === "approved") {
    const [[clearance]] = await pool.query("SELECT * FROM clearances WHERE id = ?", [step.clearance_id]);
    const studentId = clearance ? clearance.student_id : null;

    const [[{ unresolvedCount }]] = await pool.query(
      "SELECT COUNT(*) as unresolvedCount FROM department_records WHERE student_id = ? AND department_id = ? AND status = 'unresolved'",
      [studentId, step.department_id]
    );

    if (unresolvedCount > 0) {
      throw Object.assign(
        new Error("Cannot clear this student — they have unresolved records in this department"),
        { status: 400 }
      );
    }
  }
  
  const comment = remarks || "";
  

  await pool.query(
    "UPDATE clearance_steps SET status = ?, comment = ?, decided_at = NOW(6) WHERE id = ?",
    [newStatus, comment, stepId]
  );

  await logAudit(
    actorEmail,
    `${rawStatus.toUpperCase()}_STEP`,
    `Step #${stepId} marked as ${rawStatus} by ${actorEmail} — Comment: ${comment.trim() === "" ? "None" : comment}`
  );

  const [[clearance]] = await pool.query("SELECT * FROM clearances WHERE id = ?", [step.clearance_id]);
  if (!clearance) return null;

  const [allSteps] = await pool.query("SELECT * FROM clearance_steps WHERE clearance_id = ?", [clearance.id]);
  const anyRejected = allSteps.some((s) => s.status === "rejected");
  const allApproved = allSteps.every((s) => s.status === "approved");
  const anyPending = allSteps.some((s) => s.status === "pending");

  let newClearanceStatus = clearance.status;
  if (anyRejected) newClearanceStatus = "pending";
  else if (allApproved) newClearanceStatus = "awaiting_final";
  else if (anyPending) newClearanceStatus = "pending";

  await pool.query("UPDATE clearances SET status = ?, updated_at = NOW(6) WHERE id = ?", [newClearanceStatus, clearance.id]);
  return newClearanceStatus;
}