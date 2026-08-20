package com.clearance.app.data.api.dto

/**
 * Request body for POST /api/admin/departments.
 * Matches admin.service.js's createDepartment destructure exactly:
 *   createDepartment({ name, code, description }, actorEmail)
 */
data class CreateDepartmentRequest(
    val name: String,
    val code: String,
    val description: String
)