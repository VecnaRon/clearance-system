//user.routes.js
import { Router } from "express"
import { auth } from "../middleware/auth.js"
import { getUsers, getMe } from "../controllers/user.controller.js"

const r = Router()
r.get("/me", auth, getMe)
r.get("/", auth, getUsers)


export default r
