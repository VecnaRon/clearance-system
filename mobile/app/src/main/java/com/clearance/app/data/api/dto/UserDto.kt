package com.clearance.app.data.api.dto

import com.google.gson.JsonElement

/**
 * Represents the "user" object returned by:
 *   - POST /api/auth/login  (confirmed in Stage 3A)
 *   - POST /api/auth/register-student  (same shape, not used yet)
 *
 * The backend returns the full `users` table row minus the password
 * column (confirmed in server/controllers/auth.controller.js:
 * `const { password: _, ...safeUser } = user`).
 *
 * Field names are kept EXACTLY as the backend sends them (snake_case),
 * per the "preserve exact backend field names" rule — no camelCase
 * renaming layer has been introduced.
 *
 * Two fields are typed as JsonElement? instead of a concrete type,
 * because their real JSON shape could not be confirmed from source:
 *
 *   - is_active: the login controller itself contains a defensive
 *     check (`Buffer.isBuffer(user.is_active) ? ... : Boolean(...)`),
 *     meaning even the backend code is not 100% sure whether MySQL
 *     driver returns this as a Buffer, a number, or a boolean.
 *   - kcse_year: no column type could be confirmed anywhere in the
 *     provided source (no live schema file matches the real tables).
 *
 * Rather than guessing a type and risking a crash on login, these two
 * are left as raw JsonElement so they parse safely no matter what
 * shape the server sends. They are not used anywhere in Stage 3B.
 */
data class UserDto(
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