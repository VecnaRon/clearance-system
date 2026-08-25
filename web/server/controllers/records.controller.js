//recordsController.js
import * as recordsService from "../services/records.service.js";
import * as adminService from "../services/admin.service.js";

export async function logRecord(req, res) {
  try {
    const result = await recordsService.logRecord(req.user.email, req.body);
    res.status(200).json(result);
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ error: err.message || "Failed to log record" });
  }
}

export async function resolveRecord(req, res) {
  try {
    const result = await recordsService.resolveRecord(req.params.id, req.user.email);
    res.status(200).json(result);
  } catch (err) {
    console.error(err);
    if (err.status === 404) return res.status(404).end();
    res.status(err.status || 500).json({ error: err.message || "Failed to resolve record" });
  }
}

export async function myLogs(req, res) {
  try {
    const rows = await recordsService.getMyLogs(req.user.email);
    res.status(200).json(rows);
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ error: err.message || "Failed to load logs" });
  }
}

export async function departmentRecords(req, res) {
  try {
    const rows = await recordsService.getDepartmentRecords(req.user.email);
    res.status(200).json(rows);
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ error: err.message || "Failed to load department records" });
  }
}

export async function studentRecords(req, res) {
  try {
    const rows = await recordsService.getStudentRecords(req.params.studentId, req.user.email);
    res.status(200).json(rows);
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ error: err.message || "Failed to load student records" });
  }
}

export async function myRecords(req, res) {
  try {
    const rows = await recordsService.getMyRecords(req.user.email);
    res.status(200).json(rows);
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ error: err.message || "Failed to load records" });
  }
}

export async function listStudents(req, res) {
  try {
    const rows = await adminService.listUsers({ role: "student", search: req.query.search });
    res.status(200).json(rows);
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ error: err.message || "Failed to load students" });
  }
}