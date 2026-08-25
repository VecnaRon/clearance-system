// controllers/clearance.controller.js
import * as clearanceService from "../services/clearanceService.js";
import { getAdvisorAnalysis } from "../services/ai_advisor_service.js";


export async function start(req, res) {
  try {
    const clearanceId = await clearanceService.startClearance(req.user.email);
    res.status(200).json({ id: clearanceId, message: "Clearance started successfully" });
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to start clearance" });
  }
}

export async function myLatest(req, res) {
  try {
    const data = await clearanceService.getLatestForStudent(req.user.email);
    res.status(200).json(data);
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to load clearance" });
  }
}

export async function approveFinal(req, res) {
  try {
    const result = await clearanceService.approveFinal(req.params.id, req.user.email);
    res.status(200).json(result);
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to approve" });
  }
}

export async function certificate(req, res) {
  try {
    const { buffer, filename } = await clearanceService.generateCertificate(req.params.id);
    res.setHeader("Content-Disposition", `attachment; filename=${filename}`);
    res.setHeader("Content-Type", "application/pdf");
    res.status(200).send(buffer);
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Error generating certificate: " + err.message });
  }
}

export async function advisor(req, res) {
  try {
    const analysis = await getAdvisorAnalysis(req.params.id, {
      email: req.user.email,
      role: req.user.role,
    });
    res.status(200).json(analysis);
  } catch (err) {
    console.error(err);
    res.status(err.status || 500).json({ message: err.message || "Failed to get advisor analysis" });
  }
}