package com.clearance.app.data.api.dto

/**
 * One entry from GET /api/admin/overview.
 * Matches services/admin.service.js's getOverview() exactly — every
 * field is always populated with a literal fallback server-side
 * ("Unknown" name, "" admission number, 0 counts), so all fields are
 * safe as non-null.
 */
data class AdminOverviewDto(
    val id: Int,
    val full_name: String,
    val admission_number: String,
    val status: String,
    val cleared_count: Int,
    val total_departments: Int
)