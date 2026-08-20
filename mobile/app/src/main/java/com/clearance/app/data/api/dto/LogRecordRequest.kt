package com.clearance.app.data.api.dto

/**
 * Request body for POST /api/records/log.
 * Matches services/records.service.js's logRecord destructure exactly:
 *   logRecord(staffEmail, { studentId, description, amount })
 * amount is nullable — StaffDashboard.js sends null when the amount
 * field is left blank.
 */
data class LogRecordRequest(
    val studentId: Int,
    val description: String,
    val amount: Double?
)