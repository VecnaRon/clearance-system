package com.clearance.app.data.api.dto

/**
 * Request body for POST /api/admin/users.
 * Matches admin.service.js's createUser destructure exactly:
 *   createUser({ full_name, email, username, role, department, password }, actorEmail)
 *
 * IMPORTANT: `department` must be the department's NAME (e.g. "Library"),
 * matching admin.service.js's actual lookup:
 *   SELECT id FROM departments WHERE LOWER(name) = LOWER(?)
 * (NOT the department's numeric id — see Part 1 disclosure: the real
 * web ManageUsers.js form actually sends the id here, which silently
 * fails to match any department. Android intentionally sends the
 * name, which is what the backend's existing, unmodified query
 * actually expects.)
 */
data class CreateUserRequest(
    val full_name: String,
    val email: String,
    val username: String,
    val role: String,
    val department: String,
    val password: String
)