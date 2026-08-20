package com.clearance.app.data.api.dto

/**
 * Request body for POST /api/department/step/:id/decision.
 *
 * Matches what the REAL web client actually sends
 * (client/src/pages/department/DeptStudentReview.js's save()):
 *   { status, remarks, has_dues, dues_amount }
 *
 * NOTE: server/services/department.service.js's decideStep() only
 * destructures { status, remarks } from the body — has_dues and
 * dues_amount are sent for request-shape parity with the real web
 * app but are NOT read or persisted server-side. Android sends them
 * as false/0, their only real value anywhere in this flow, since no
 * UI (web or Android) has any way to make the backend act on them.
 */
data class DepartmentDecisionRequest(
    val status: String,
    val remarks: String,
    val has_dues: Boolean,
    val dues_amount: Double
)