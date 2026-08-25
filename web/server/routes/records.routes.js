//recordsRoutes.js
import { Router } from "express"
import { auth } from "../middleware/auth.js"
import { allow } from "../middleware/roles.js"
import {
  logRecord,
  resolveRecord,
  myLogs,
  departmentRecords,
  studentRecords,
  myRecords,
  listStudents,
} from "../controllers/records.controller.js"

const r = Router()
r.post("/log", auth, allow("staff", "admin"), logRecord)
r.put("/:id/resolve", auth, allow("staff", "admin"), resolveRecord)
r.get("/my-logs",auth, allow("staff", "admin"), myLogs)
r.get("/department", auth, allow("hod", "admin"), departmentRecords)
r.get("/student/:studentId", auth, allow("staff", "hod", "admin"), studentRecords)
r.get("/mine", auth, allow("student", "admin"), myRecords)
r.get("/students", auth, allow("staff", "hod", "admin"), listStudents)

export default r