package com.clearance.app.data.api.dto

import com.google.gson.JsonElement

/**
 * One entry from GET /api/admin/pending-users.
 * Matches services/admin.service.js's getPendingUsers() exactly.
 *
 * is_active is kept as JsonElement? for the same reason as UserDto.kt
 * and StudentMeDto.kt — admin.service.js's own normalizeActive()
 * helper defensively checks Buffer.isBuffer(row.is_active), meaning
 * even the backend isn't fully certain of this column's JSON shape.
 */
data class PendingUserDto(
    val id: Int,
    val full_name: String,
    val email: String,
    val admission_number: String,
    val is_active: JsonElement?
)