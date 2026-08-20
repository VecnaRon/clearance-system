package com.clearance.app.data.api.dto

/**
 * One entry from GET /api/admin/audit-logs.
 * Matches services/admin.service.js's getAuditLogs() exactly.
 *
 * actor/action/details/timestamp are nullable: no guarantee every
 * historical row has non-empty values for these (multiple call
 * sites across the codebase call logAudit() with varying arguments),
 * and Gson does not enforce Kotlin null-safety when deserializing —
 * a null JSON value silently lands in a "non-null" Kotlin field
 * otherwise, which can crash at render time. Screens must render
 * these with a fallback (e.g. "\u2014") rather than assume non-null.
 */
data class AuditLogDto(
    val id: Int,
    val actor: String?,
    val action: String?,
    val details: String?,
    val timestamp: String?
)