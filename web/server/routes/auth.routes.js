import express from "express";
import {
  login,
  registerStudent,
  logout,
  me,
} from "../controllers/auth.controller.js";

import { auth } from "../middleware/auth.js";

const router = express.Router();

/*
|--------------------------------------------------------------------------
| Public Routes
|--------------------------------------------------------------------------
*/

router.post("/login", login);
router.post("/register-student", registerStudent);

/*
|--------------------------------------------------------------------------
| Protected Routes
|--------------------------------------------------------------------------
*/

router.get("/me", auth, me);

router.post("/logout", auth, logout);

export default router;