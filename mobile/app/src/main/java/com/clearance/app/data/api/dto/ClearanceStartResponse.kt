package com.clearance.app.data.api.dto

/**
 * Response for a successful POST /api/clearance/start (HTTP 200).
 * Confirmed verbatim in clearance.controller.js:
 *   res.status(200).json({ id: clearanceId, message: "Clearance started successfully" })
 */
data class ClearanceStartResponse(
    val id: Int,
    val message: String
)