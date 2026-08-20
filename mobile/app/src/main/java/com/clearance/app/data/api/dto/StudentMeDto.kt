package com.clearance.app.data.api.dto

import com.google.gson.JsonElement

/**
 * Response for GET /api/student/me.
 *
 * Matches server/controllers/student.controller.js's explicit SELECT
 * list exactly:
 *   SELECT id, username, email, role, department_id, full_name, phone,
 *          admission_number, kcse_year, class_form, stream, is_active
 *   FROM users WHERE id = ?
 *
 * is_active and kcse_year are kept as JsonElement? for the same
 * reason as UserDto.kt — their real JSON shape (Buffer vs number vs
 * boolean/string) could not be confirmed from source, and guessing a
 * concrete type risks a crash on this screen's very first API call.
 */
data class StudentMeDto(
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
    val is_active: JsonElement?
)