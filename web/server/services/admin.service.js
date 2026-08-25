//admin.service.js
import { pool } from "../config/db.js";
import bcrypt from "bcryptjs";

async function logAudit(actorEmail, action, details) {
  await pool.query(
    "INSERT INTO audit_logs (action, actor_email, created_at, details) VALUES (?, ?, NOW(6), ?)",
    [action, actorEmail, details]
  );
}

function normalizeActive(row) {
  if (row && Buffer.isBuffer(row.is_active)) row.is_active = row.is_active[0] === 1;
  return row;
}

function formatTimestamp(dt) {
  const d = new Date(dt);
  const months = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];
  const pad = (n) => String(n).padStart(2, "0");
  return `${pad(d.getDate())} ${months[d.getMonth()]} ${d.getFullYear()}, ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

export async function getOverview() {
  const [departments] = await pool.query("SELECT * FROM departments");
  const totalDepartments = departments.length;

  const [clearances] = await pool.query("SELECT * FROM clearances");
  const result = [];
  for (const c of clearances) {
    const [[student]] = await pool.query("SELECT * FROM users WHERE id = ?", [c.student_id]);
    const [steps] = await pool.query("SELECT * FROM clearance_steps WHERE clearance_id = ?", [c.id]);
    const clearedCount = steps.filter((s) => s.status === "approved").length;

    result.push({
      id: c.id,
      full_name: student ? student.full_name : "Unknown",
      admission_number: student && student.admission_number ? student.admission_number : "",
      status: c.status,
      cleared_count: clearedCount,
      total_departments: totalDepartments,
    });
  }
  return result;
}

export async function listDepartments() {
  const [rows] = await pool.query("SELECT * FROM departments");
  return rows;
}

export async function createDepartment({ name, code, description }, actorEmail) {
  const [[{ count }]] = await pool.query("SELECT COUNT(*) as count FROM departments WHERE name = ?", [name]);
  if (count > 0) {
    throw Object.assign(new Error("Department already exists"), { status: 400 });
  }

  const finalCode = (code || "").toUpperCase();
  await pool.query(
    "INSERT INTO departments (name, code, is_active, description) VALUES (?, ?, 1, ?)",
    [name, finalCode, description || ""]
  );

  await logAudit(actorEmail, "CREATED_DEPARTMENT", `Department created: ${name} (${finalCode})`);
  return { message: "Department created successfully" };
}

// NOTE: matches Spring exactly — only name/description are updatable here.
// code and is_active are intentionally NOT touched, even if sent in the body.
export async function updateDepartment(id, { name, description }, actorEmail) {
  const [[dept]] = await pool.query("SELECT * FROM departments WHERE id = ?", [id]);
  if (!dept) throw Object.assign(new Error("Department not found"), { status: 404 });

  const newName = name !== undefined ? name : dept.name;
  const newDescription = description !== undefined ? description : dept.description;

  await pool.query("UPDATE departments SET name = ?, description = ? WHERE id = ?", [newName, newDescription, id]);
  await logAudit(actorEmail, "UPDATED_DEPARTMENT", `Department updated: ID ${id}`);
  return { message: "Department updated successfully" };
}

// NOTE: matches Spring exactly — no guard against existing clearance_steps referencing this department.
export async function deleteDepartment(id, actorEmail) {
  const [[dept]] = await pool.query("SELECT * FROM departments WHERE id = ?", [id]);
  if (!dept) throw Object.assign(new Error("Department not found"), { status: 404 });

  await pool.query("DELETE FROM departments WHERE id = ?", [id]);
  await logAudit(actorEmail, "DELETED_DEPARTMENT", `Department deleted: ID ${id}`);
  return { message: "Department deleted successfully" };
}

export async function listUsers({ role, search }) {
  let sql = "SELECT * FROM users WHERE is_active = 1";
  const params = [];

  if (role) {
    sql += " AND role = ?";
    params.push(role);
  }
  if (search) {
    sql += " AND (LOWER(full_name) LIKE ? OR LOWER(admission_number) LIKE ?)";
    const term = `%${search.toLowerCase()}%`;
    params.push(term, term);
  }

  const [rows] = await pool.query(sql, params);
  const result = [];
  for (const u of rows) {
    let deptName = "";
    if (u.department_id) {
      const [[d]] = await pool.query("SELECT name FROM departments WHERE id = ?", [u.department_id]);
      deptName = d ? d.name : "";
    }
    result.push({
      id: u.id,
      full_name: u.full_name,
      email: u.email,
      username: u.username || "",
      role: u.role,
      department: deptName,
      admission_number: u.admission_number || "",
      class_form: u.class_form || "",
      stream: u.stream || "",
      is_active: normalizeActive(u).is_active,
    });
  }
  return result;
}

export async function createUser({ full_name, email, username, role, department, password }, actorEmail) {
  const [[{ count }]] = await pool.query("SELECT COUNT(*) as count FROM users WHERE email = ?", [email]);
  if (count > 0) {
    throw Object.assign(new Error("Email already in use"), { status: 400 });
  }

  let departmentId = null;
  if (department) {
    const [[d]] = await pool.query("SELECT id FROM departments WHERE LOWER(name) = LOWER(?)", [department]);
    if (d) departmentId = d.id;
  }

  const passwordHash = await bcrypt.hash(password, 10);
  const finalUsername = username || email;

  await pool.query(
    `INSERT INTO users (full_name, email, username, password, role, department_id, is_active)
     VALUES (?, ?, ?, ?, ?, ?, 1)`,
    [full_name, email, finalUsername, passwordHash, role, departmentId]
  );

  await logAudit(actorEmail, "CREATED_USER", `User created: ${full_name} (${role})`);
  return { message: "User created successfully" };
}

export async function updateUser(id, { full_name, email, username, role, department, password }, actorEmail) {
  const [[user]] = await pool.query("SELECT * FROM users WHERE id = ?", [id]);
  if (!user) throw Object.assign(new Error("User not found"), { status: 404 });

  const fields = { full_name: user.full_name, email: user.email, username: user.username, role: user.role };
  if (full_name !== undefined) fields.full_name = full_name;
  if (email !== undefined) fields.email = email;
  if (username !== undefined) fields.username = username;
  if (role !== undefined) fields.role = role;

  let departmentId = user.department_id;
  if (department !== undefined) {
    if (department.trim() === "") {
      departmentId = null;
    } else {
      const [[d]] = await pool.query("SELECT id FROM departments WHERE LOWER(name) = LOWER(?)", [department]);
      if (d) departmentId = d.id;
    }
  }

  let sql = "UPDATE users SET full_name = ?, email = ?, username = ?, role = ?, department_id = ?";
  const params = [fields.full_name, fields.email, fields.username, fields.role, departmentId];

  if (password && password.trim() !== "") {
    const hash = await bcrypt.hash(password, 10);
    sql += ", password = ?";
    params.push(hash);
  }

  sql += " WHERE id = ?";
  params.push(id);
  await pool.query(sql, params);

  await logAudit(actorEmail, "UPDATED_USER", `User updated: ID ${fields.full_name} (ID: ${id})`);
  return { message: "User updated successfully" };
}

export async function deleteUser(id, actorEmail) {
  const [[user]] = await pool.query("SELECT * FROM users WHERE id = ?", [id]);
  if (!user) throw Object.assign(new Error("User not found"), { status: 404 });

  const [clearances] = await pool.query("SELECT id FROM clearances WHERE student_id = ?", [id]);
  for (const c of clearances) {
    await pool.query("DELETE FROM clearance_steps WHERE clearance_id = ?", [c.id]);
    await pool.query("DELETE FROM clearances WHERE id = ?", [c.id]);
  }
  await pool.query("DELETE FROM department_records WHERE student_id = ?", [id]);
  await pool.query("DELETE FROM users WHERE id = ?", [id]);

  await logAudit(actorEmail, "DELETED_USER", `User deleted: ID ${id}`);
  return { message: "User deleted successfully" };
}

export async function getPendingUsers() {
  const [rows] = await pool.query(
    "SELECT * FROM users WHERE role = 'student' AND is_active = 0"
  );
  return rows.map((u) => ({
    id: u.id,
    full_name: u.full_name,
    email: u.email,
    admission_number: u.admission_number || "",
    is_active: normalizeActive(u).is_active,
  }));
}

export async function activateUser(id, actorEmail) {
  const [[user]] = await pool.query("SELECT * FROM users WHERE id = ?", [id]);
  if (!user) throw Object.assign(new Error("User not found"), { status: 404 });

  await pool.query("UPDATE users SET is_active = 1 WHERE id = ?", [id]);
  await logAudit(actorEmail, "ACTIVATED_USER", `Account activated for: ${user.full_name} (${user.email})`);
  return { message: `Account for ${user.full_name} activated successfully` };
}

export async function deactivateUser(id, actorEmail) {
  const [[user]] = await pool.query("SELECT * FROM users WHERE id = ?", [id]);
  if (!user) throw Object.assign(new Error("User not found"), { status: 404 });

  if (user.role === "admin") {
    throw Object.assign(new Error("Cannot deactivate an admin account"), { status: 400 });
  }

  await pool.query("UPDATE users SET is_active = 0 WHERE id = ?", [id]);
  await logAudit(actorEmail, "DEACTIVATED_USER", `Account deactivated for: ${user.full_name} (${user.email})`);
  return { message: `Account for ${user.full_name} deactivated successfully` };
}

export async function getAuditLogs() {
  const [rows] = await pool.query("SELECT * FROM audit_logs ORDER BY created_at DESC LIMIT 50");
  return rows.map((log) => ({
    id: log.id,
    actor: log.actor_email,
    action: log.action,
    details: log.details,
    timestamp: formatTimestamp(log.created_at),
  }));
}

export async function reportClearanceSummary() {
  const [departments] = await pool.query("SELECT * FROM departments");
  const report = [];
  for (const dept of departments) {
    const [[{ pending }]] = await pool.query(
      "SELECT COUNT(*) as pending FROM clearance_steps WHERE department_id = ? AND status = 'pending'", [dept.id]
    );
    const [[{ approved }]] = await pool.query(
      "SELECT COUNT(*) as approved FROM clearance_steps WHERE department_id = ? AND status = 'approved'", [dept.id]
    );
    const [[{ rejected }]] = await pool.query(
      "SELECT COUNT(*) as rejected FROM clearance_steps WHERE department_id = ? AND status = 'rejected'", [dept.id]
    );

    report.push({
      department_name: dept.name,
      department_code: dept.code || "",
      cleared: approved,
      rejected,
      pending,
    });
  }
  return report;
}