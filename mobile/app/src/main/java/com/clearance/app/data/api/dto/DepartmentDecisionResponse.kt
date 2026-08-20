package com.clearance.app.data.api.dto

/**
 * Response for POST /api/department/step/:id/decision.
 * Matches controllers/department.controller.js's decide() exactly:
 *   res.status(200).json({ message: "Decision recorded successfully", clearanceStatus: newStatus })
 *
 * clearanceStatus is nullable: department.service.js's decideStep()
 * returns null in one edge case (parent clearance row not found),
 * which the controller would then serialize as JSON null.
 */
data class DepartmentDecisionResponse(
    val message: String,
    val clearanceStatus: String?
)