//user.model.js
import { pool } from "../config/db.js"

export async function findByEmail(email) {
  const [rows] = await pool.query("SELECT * FROM users WHERE email = ?", [email])
  return rows[0]
}

export async function findById(id) {
  const [rows] = await pool.query("SELECT * FROM users WHERE id = ?", [id])
  return rows[0]
}

export async function createUser(u) {
  const { username, email, password, role, department_id, full_name, phone } = u
  const [res] = await pool.query(
    `INSERT INTO users (username,email,password,role,department_id,full_name,phone)
     VALUES (?,?,?,?,?,?,?)`,
    [username, email, password, role, department_id || null, full_name, phone || null],
  )
  return { id: res.insertId, ...u }
}

export async function listUsers() {
  const [rows] = await pool.query("SELECT * FROM users ORDER BY id DESC")
  return rows
}

export async function findByIdWithDepartment(id) {
  const [rows] = await pool.query(
    `SELECT u.*, d.name AS department_name
     FROM users u
     LEFT JOIN departments d ON u.department_id = d.id
     WHERE u.id = ?`,
    [id]
  )
  return rows[0]
}