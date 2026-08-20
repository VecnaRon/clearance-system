package com.clearance.app.data.api.dto

/**
 * Request body for POST /api/auth/login.
 *
 * Matches server/controllers/auth.controller.js exactly:
 *   const { email, password } = req.body
 *
 * No fields added, none renamed.
 */
data class LoginRequest(
    val email: String,
    val password: String
)
