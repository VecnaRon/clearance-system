package com.clearance.app.data.api.dto

import com.google.gson.JsonElement

/**
 * Response for GET /api/users/me.
 * Matches models/user.model.js's findByIdWithDepartment(id) exactly:
 *   SELECT u.*, d.name AS department_name FROM users u
 *   LEFT JOIN departments d ON u.department_id = d.id WHERE u.id = ?
 *
 * kcse_year/is_active kept as JsonElement? for the same reason as
 * UserDto.kt/StudentMeDto.kt — same ambiguous columns, same u.* select.
 * department_name is nullable: LEFT JOIN yields null when the user
 * has no department_id.
 */
data class UserProfileDto(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val department_id: Int?,
    val full_name: String?,
    val phone: String?,
    val admission_number: String?,
    val kcse_year: JsonElement?,
    val class_form: String?,
    val stream: String?,
    val is_active: JsonElement?,
    val department_name: String?
)