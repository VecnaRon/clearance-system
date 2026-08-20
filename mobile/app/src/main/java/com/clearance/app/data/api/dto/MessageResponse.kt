package com.clearance.app.data.api.dto

/**
 * Generic {"message": "..."} response shape, confirmed identical
 * across: PUT /admin/users/:id/activate, POST /clearance/:id/final-approve,
 * POST/PUT/DELETE /admin/departments. Shared instead of duplicated
 * per-endpoint since the shape is genuinely the same.
 */
data class MessageResponse(
    val message: String
)