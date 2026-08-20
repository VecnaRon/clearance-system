package com.clearance.app.data.api.dto

/**
 * Request body for POST /api/clearance/start.
 *
 * Matches client/src/pages/student/ClearanceForm.js exactly:
 *   api.post("/clearance/start", { reason, reason_other })
 *
 * NOTE: server/controllers/clearance.controller.js's start() function
 * never reads req.body at all — clearanceService.startClearance(email)
 * takes only the authenticated user's email. These two fields are
 * sent for UI/behavioral parity with the real web app (per earlier
 * approved decision) but are NOT persisted anywhere server-side.
 */
data class ClearanceStartRequest(
    val reason: String,
    val reason_other: String
)
