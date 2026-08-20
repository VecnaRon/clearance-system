package com.clearance.app.data.api.dto

import com.google.gson.JsonElement

/**
 * Response shape shared by GET /api/admin/users and GET /api/records/students
 * (the latter reuses admin.service.js's listUsers() internally — confirmed
 * in records.controller.js's listStudents()).
 *
 * class_form/stream made nullable: listUsers() pushes u.class_form/u.stream
 * straight from the raw column with no fallback, unlike full_name/admission_number
 * which do have "" fallbacks. A student row with NULL class_form/stream in MySQL
 * would otherwise deserialize a JSON null into a "non-null" Kotlin String via
 * Gson (which does not enforce Kotlin null-safety), crashing the first time
 * that field is used — this was the secondary risk found alongside the
 * StaffDashboardScreen crash.
 */
data class AdminUserDto(
    val id: Int,
    val full_name: String,
    val email: String,
    val username: String,
    val role: String,
    val department: String,
    val admission_number: String,
    val class_form: String?,
    val stream: String?,
    val is_active: JsonElement?
)