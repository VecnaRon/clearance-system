//auth.controller.js
import bcrypt from "bcryptjs"
import jwt from "jsonwebtoken"
import { env } from "../config/env.js"
import { pool } from "../config/db.js"
//import { createUser, findByEmail } from "../models/user.model.js"
//import { createStudent } from "../models/student.model.js"
import { findByEmail } from "../models/user.model.js"

function sign(user) {
  const payload = {
  id: user.id,
  username: user.username,
  email: user.email,
  role: user.role,
  department_id: user.department_id,
}
  return jwt.sign(payload, env.jwtSecret, { expiresIn: "7d" })

}
 //login
export async function login(req, res) {
  const { email, password } = req.body
  const user = await findByEmail(email)
  if (!user) return res.status(400).json({ message: "Invalid credentials" })
  
  const isActive = Buffer.isBuffer(user.is_active)
  ? user.is_active[0] === 1
  : Boolean(user.is_active);

  if (!isActive) {
    return res.status(403).json({
      message: "Account is inactive. Contact the administrator.",
    });
  }

  const ok = await bcrypt.compare(password, user.password)

  if (!ok) return res.status(400).json({ message: "Invalid credentials" })

  const token = sign(user)
  const { password: _, ...safeUser } = user;
  res.json({ token, user: safeUser, });
}

export async function registerStudent(req, res) {
  const connection = await pool.getConnection();

  try {
    await connection.beginTransaction();

    const {
      full_name,
      admission_number,
      kcse_year,
      class_form,
      stream,
      phone,
      email,
      password,
    } = req.body;

    const username = admission_number;

    const [exists] = await connection.query(
      "SELECT id FROM users WHERE email = ? OR username = ?",
      [email, username]
    );

    if (exists.length) {
      await connection.rollback();
      return res.status(400).json({
        message: "Email or Admission Number already exists",
      });
    }

    const hash = await bcrypt.hash(password, 10);

    const [userResult] = await connection.query(
      `INSERT INTO users
      (username,email,password,role,department_id,full_name,phone,admission_number,
    kcse_year,
    class_form,
    stream,
    is_active)
      VALUES (?,?,?,?,?,?,?,?,?,?,?,0)`,
      [
        username,
        email,
        hash,
        "student",
        null,
        full_name,
        phone,
        admission_number,
        kcse_year,
        class_form,
        stream,
      ]
    );

    const userId = userResult.insertId;

    /*await connection.query(
      `INSERT INTO users
      (user_id,admission_number,full_name,kcse_year,class_form,stream,phone,email)
      VALUES (?,?,?,?,?,?,?,?)`,
      [
        userId,
        admission_number,
        full_name,
        kcse_year,
        class_form,
        stream,
        phone,
        email,
      ]
    );*/

    await connection.commit();

    const user = {
      id: userId,
      username,
      email,
      role: "student",
      department_id: null,
      full_name,
      phone,
      admission_number,
      kcse_year,
      class_form,
      stream,
      is_active: true,
    };

    const token = sign(user);

    res.status(201).json({
      token,
      user,
    });

  } catch (err) {

    await connection.rollback();

    console.error(err);

    res.status(500).json({
      message: "Registration failed",
    });

  } finally {

    connection.release();

  }
}

// Get currently authenticated user
export async function me(req, res) {
  res.status(200).json(req.user);
}

// Logout (JWT is stateless)
export async function logout(req, res) {
  res.status(200).json({
    message: "Logged out successfully",
  });
}
