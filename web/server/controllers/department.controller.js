//departmentController.js.
import * as departmentService from "../services/department.service.js";

export async function queue(req, res) {
  try {
    const rows = await departmentService.queueForHod(req.user.email);
    res.status(200).json(rows);
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to load queue" });
  }
}

export async function stepDetail(req, res) {
  try {
    const step = await departmentService.getStepDetail(req.params.id);
    res.status(200).json(step);
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to load step" });
  }
}

export async function decide(req, res) {
  try {
    const newStatus = await departmentService.decideStep(req.params.id, req.body, req.user.email);
    res.status(200).json({ message: "Decision recorded successfully", clearanceStatus: newStatus });
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to process decision" });
  }
}