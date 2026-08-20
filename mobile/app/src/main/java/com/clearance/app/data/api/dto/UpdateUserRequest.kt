package com.clearance.app.data.api.dto

/**
 * Request body for PUT /api/admin/users/:id.
 * Matches admin.service.js's updateUser destructure exactly:
 *   updateUser(id, { full_name, email, username, role, department, password }, actorEmail)
 *
 * password is nullable/blank-tolerant: updateUser() only re-hashes
 * and updates the password if it is non-blank, otherwise leaves the
 * existing password untouched — matching ManageUsers.js's "leave
 * blank to keep current" behavior exactly.
 *
 * department: same NAME-not-id rule as CreateUserRequest.
 */
data class UpdateUserRequest(
    val full_name: String,
    val email: String,
    val username: String,
    val role: String,
    val department: String,
    val password: String
)