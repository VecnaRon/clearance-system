//user.controller.js
import { listUsers, findByIdWithDepartment } from "../models/user.model.js"

export async function getUsers(req, res) {
  const rows = await listUsers()
  res.json(rows)
}

export async function getMe(req, res) {
  try {
    const me = await findByIdWithDepartment(req.user.id)
    if (!me) return res.status(404).json({ message: "User not found" })
    delete me.password
    res.json(me)
  } catch (err) {
    console.error(err)
    res.status(500).json({ message: "Failed to load profile" })
  }
}