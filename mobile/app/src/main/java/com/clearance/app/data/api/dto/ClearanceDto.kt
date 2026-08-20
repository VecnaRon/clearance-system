package com.clearance.app.data.api.dto

/**
 * Response for GET /api/clearance/my-latest.
 *
 * The backend returns one of two different shapes for this same
 * endpoint (confirmed in clearanceService.js's getLatestForStudent):
 *
 *   No clearance yet (still HTTP 200): { "message": "No clearance found" }
 *   Clearance exists:                  { id, admission_number, status,
 *                                         submitted_at, departments: [...] }
 *
 * All fields are nullable here so a single DTO safely deserializes
 * either shape — id == null is exactly how the real web app detects
 * "no active clearance" (ClearanceForm.js: `if (clearanceData && clearanceData.id)`),
 * so that same check is reused on the Android side.
 */
data class ClearanceDto(
    val id: Int?,
    val admission_number: String?,
    val status: String?,
    val submitted_at: String?,
    val departments: List<ClearanceDepartmentDto>?,
    val message: String?
)