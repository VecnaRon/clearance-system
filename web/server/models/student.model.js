//student.model.js
import { pool } from "../config/db.js"

export async function createStudent(userId, payload) {
  return { id: userId, ...payload };
}

export async function findStudentByUserId(userId) {
  const [rows] = await pool.query(
    `SELECT
        id,
        username,
        email,
        role,
        full_name,
        admission_number,
        kcse_year,
        class_form,
        stream,
        phone,
        department_id,
        is_active
     FROM users
     WHERE id = ? AND role = 'student'`,
    [userId]
  );

  const student = rows[0];
  if (!student) return null;

  // Normalize BIT(1) -> boolean
  if (Buffer.isBuffer(student.is_active)) {
    student.is_active = student.is_active[0] === 1;
  }

  return student;
}

export async function findStudentByAdmission(adm) {
  const [rows] = await pool.query(
    `SELECT
        id,
        username,
        email,
        role,
        full_name,
        admission_number,
        kcse_year,
        class_form,
        stream,
        phone,
        department_id,
        is_active
     FROM users
     WHERE admission_number = ? AND role = 'student'`,
    [adm]
  );

  const student = rows[0];
  if (!student) return null;

  if (Buffer.isBuffer(student.is_active)) {
    student.is_active = student.is_active[0] === 1;
  }

  return student;
}