package com.clearance.app.data.api.dto

/**
 * Request body for PUT /api/admin/departments/:id.
 * Matches admin.service.js's updateDepartment destructure exactly:
 *   updateDepartment(id, { name, description }, actorEmail)
 *
 * `code` and `is_active` are intentionally absent — the backend
 * ignores both fields on this endpoint even if sent (confirmed by an
 * explicit comment in admin.service.js), so this DTO does not offer
 * fields the backend can't actually update.
 */
data class UpdateDepartmentRequest(
    val name: String,
    val description: String
)