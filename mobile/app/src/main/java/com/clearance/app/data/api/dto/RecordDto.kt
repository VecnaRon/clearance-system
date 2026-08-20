package com.clearance.app.data.api.dto

/**
 * One entry returned by GET /api/records/mine.
 *
 * Matches services/records.service.js's mapRecord() exactly —
 * deliberately camelCase, unlike ClearanceDto's snake_case. This is
 * not a mistake: the backend itself is inconsistent between the
 * clearance endpoints (snake_case) and the records endpoints
 * (camelCase), confirmed directly in source. Both are preserved
 * exactly as returned rather than being normalized to one convention.
 */
data class RecordDto(
    val id: Int,
    val studentId: Int?,
    val studentName: String?,
    val studentAdmission: String?,
    val departmentId: Int?,
    val loggedBy: Int?,
    val description: String?,
    val amount: Double?,
    val status: String,
    val resolvedBy: Int?,
    val resolvedAt: String?,
    val createdAt: String?,
    val updatedAt: String?
)