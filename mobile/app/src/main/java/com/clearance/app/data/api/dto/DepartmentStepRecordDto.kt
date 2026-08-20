package com.clearance.app.data.api.dto

/**
 * One entry in DepartmentStepDetailDto.records[].
 * Matches services/department.service.js's getStepDetail() record
 * mapping exactly — a DIFFERENT shape from RecordDto (used by
 * /records/mine): no studentId/departmentId/resolvedAt here, because
 * this endpoint's query and mapping are genuinely different code.
 */
data class DepartmentStepRecordDto(
    val id: Int,
    val description: String,
    val amount: Double?,
    val status: String,
    val createdAt: String,
    val loggedBy: String
)