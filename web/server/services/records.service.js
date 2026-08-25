//recordsService.js
import { pool } from "../config/db.js";

async function logAudit(actorId, action, details) {
  await pool.query(
    "INSERT INTO audit_logs (action, actor_email, created_at, details) VALUES (?, ?, NOW(6), ?)",
    [action, String(actorId), details]
  );
}

function mapRecord(r) {
  return {
    id: r.id,
    studentId: r.student_id,
    studentName: r.student_name,           // ← NEW
    studentAdmission: r.student_admission,
    departmentId: r.department_id,
    loggedBy: r.logged_by,
    description: r.description,
    amount: r.amount,
    status: r.status,
    resolvedBy: r.resolved_by,
    resolvedAt: r.resolved_at,
    createdAt: r.created_at,
    updatedAt: r.updated_at,
  };
}

export async function logRecord(staffEmail, { studentId, description, amount }) {
  const [[staff]] = await pool.query("SELECT * FROM users WHERE email = ?", [staffEmail]);
  if (!staff) throw Object.assign(new Error("Unauthorized"), { status: 401 });
  if (!staff.department_id) {
    throw Object.assign(new Error("You are not assigned to any department"), { status: 400 });
  }

  const sId = Number(studentId);
  const [[student]] = await pool.query("SELECT * FROM users WHERE id = ?", [sId]);
  if (!student || student.role !== "student") {
    throw Object.assign(new Error("Student not found"), { status: 400 });
  }

  const amt = amount !== undefined && amount !== null && String(amount).trim() !== "" ? Number(amount) : null;

  await pool.query(
    `INSERT INTO department_records (student_id, department_id, logged_by, description, amount, status, created_at)
     VALUES (?, ?, ?, ?, ?, 'unresolved', NOW(6))`,
    [sId, staff.department_id, staff.id, description, amt]
  );

  await logAudit(
    staff.id,
    "RECORD_LOGGED",
    `Staff ${staff.full_name} logged record for student ID ${sId}: ${description}`
  );

  return { message: "Record logged successfully" };
}

export async function resolveRecord(recordId, staffEmail) {
  const [[staff]] = await pool.query("SELECT * FROM users WHERE email = ?", [staffEmail]);
  if (!staff) throw Object.assign(new Error("Unauthorized"), { status: 401 });

  const [[record]] = await pool.query("SELECT * FROM department_records WHERE id = ?", [recordId]);
  if (!record) throw Object.assign(new Error("Not found"), { status: 404 });

  if (record.department_id !== staff.department_id) {
    throw Object.assign(new Error("You can only resolve records in your department"), { status: 403 });
  }

  await pool.query(
    "UPDATE department_records SET status = 'resolved', resolved_by = ?, resolved_at = NOW(6) WHERE id = ?",
    [staff.id, recordId]
  );

  await logAudit(
    staff.id,
    "RECORD_RESOLVED",
    `Staff ${staff.full_name} resolved record ID ${recordId} for student ID ${record.student_id}`
  );

  return { message: "Record marked as resolved" };
}

export async function getMyLogs(staffEmail) {
  const [[staff]] = await pool.query("SELECT * FROM users WHERE email = ?", [staffEmail]);
  if (!staff) throw Object.assign(new Error("Unauthorized"), { status: 401 });

  const [rows] = await pool.query(
  `${RECORD_WITH_STUDENT_SELECT} WHERE dr.logged_by = ?`,
  [staff.id]
  );
  return rows.map(mapRecord);
}

export async function getDepartmentRecords(hodEmail) {
  const [[hod]] = await pool.query("SELECT * FROM users WHERE email = ?", [hodEmail]);
  if (!hod) throw Object.assign(new Error("Unauthorized"), { status: 401 });
  if (!hod.department_id) {
    throw Object.assign(new Error("You are not assigned to any department"), { status: 400 });
  }

  const [rows] = await pool.query(
  `${RECORD_WITH_STUDENT_SELECT} WHERE dr.department_id = ?`,
  [hod.department_id]
);
  return rows.map(mapRecord);
}

export async function getStudentRecords(studentId, hodEmail) {
  const [[hod]] = await pool.query("SELECT * FROM users WHERE email = ?", [hodEmail]);
  if (!hod) throw Object.assign(new Error("Unauthorized"), { status: 401 });
  if (!hod.department_id) {
    throw Object.assign(new Error("You are not assigned to any department"), { status: 400 });
  }

  const [rows] = await pool.query(
    "SELECT * FROM department_records WHERE student_id = ? AND department_id = ?",
    [studentId, hod.department_id]
  );
  return rows.map(mapRecord);
}

export async function getMyRecords(studentEmail) {
  const [[student]] = await pool.query("SELECT * FROM users WHERE email = ?", [studentEmail]);
  if (!student) throw Object.assign(new Error("Unauthorized"), { status: 401 });

  const [rows] = await pool.query("SELECT * FROM department_records WHERE student_id = ?", [student.id]);
  return rows.map(mapRecord);
}

const RECORD_WITH_STUDENT_SELECT = `
  SELECT dr.*, u.full_name AS student_name, u.admission_number AS student_admission
  FROM department_records dr
  JOIN users u ON u.id = dr.student_id
`;