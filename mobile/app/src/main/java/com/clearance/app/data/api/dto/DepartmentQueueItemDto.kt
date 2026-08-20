package com.clearance.app.data.api.dto

/**
 * One entry from GET /api/department/queue.
 * Matches services/department.service.js's queueForHod() exactly.
 * All fields always populated with fallback defaults server-side
 * ("Unknown" name, "" admission number), so all non-null.
 */
data class DepartmentQueueItemDto(
    val clearance_step_id: Int,
    val clearanceId: Int,
    val full_name: String,
    val admission_number: String,
    val status: String
)