//student.controller.js
import { pool } from "../config/db.js";

export async function me(req, res) {
  const [[user]] = await pool.query(
    `SELECT id, username, email, role, department_id, full_name, phone,
            admission_number, kcse_year, class_form, stream, is_active
     FROM users WHERE id = ?`,
    [req.user.id]
  );
  if (!user) return res.status(404).json({ message: "User not found" });
  res.json(user);
}
