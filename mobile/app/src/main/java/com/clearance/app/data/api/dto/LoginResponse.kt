package com.clearance.app.data.api.dto

/**
 * Response body for a SUCCESSFUL POST /api/auth/login (HTTP 200).
 *
 * Confirmed shape from server/controllers/auth.controller.js:
 *   res.json({ token, user: safeUser })
 */
data class LoginResponse(
    val token: String,
    val user: UserDto
)