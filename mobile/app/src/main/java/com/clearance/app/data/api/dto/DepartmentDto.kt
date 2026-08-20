package com.clearance.app.data.api.dto

import com.google.gson.JsonElement

/**
 * One entry from GET /api/admin/departments.
 * Matches services/admin.service.js's listDepartments() — a raw
 * `SELECT * FROM departments`.
 *
 * is_active is JsonElement? rather than Boolean: unlike users.is_active,
 * this column is never passed through admin.service.js's
 * normalizeActive() helper at all — listDepartments() returns the raw
 * row untouched — so its real JSON shape is even less certain here
 * than on the users endpoints. Not guessing a concrete type avoids a
 * crash on this screen's very first load.
 */
data class DepartmentDto(
    val id: Int,
    val name: String,
    val code: String?,
    val description: String?,
    val is_active: JsonElement?
)