package com.clearance.app.data.api.dto

import com.google.gson.JsonElement

/**
 * One entry from GET /api/admin/reports/clearance-summary.
 * Matches services/admin.service.js's reportClearanceSummary() exactly.
 *
 * cleared/rejected/pending are kept as JsonElement? rather than Int:
 * the original web Reports.js itself defensively wraps every one of
 * these in Number.parseInt(...) || 0, meaning even the original
 * developers were not certain these always arrive as JSON numbers
 * (they may be delivered as numeric strings depending on the MySQL
 * driver's aggregate COUNT() typing). A JsonElement.asInt-based
 * parse (see toIntSafe() in AdminReportsScreen.kt) handles both
 * shapes safely, mirroring that same defensiveness rather than
 * guessing a type that could crash this screen's first load.
 */
data class DepartmentReportDto(
    val department_name: String,
    val department_code: String?,
    val cleared: JsonElement?,
    val rejected: JsonElement?,
    val pending: JsonElement?
)