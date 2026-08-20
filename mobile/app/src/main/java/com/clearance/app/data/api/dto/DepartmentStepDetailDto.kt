package com.clearance.app.data.api.dto

/**
 * Response for GET /api/department/step/:id.
 * Matches services/department.service.js's getStepDetail() exactly.
 * has_dues/dues_amount are always false/0 as returned by the current
 * backend (hardcoded literals in the service, not derived from any
 * table) — kept for contract parity, not because they carry real data.
 */
data class DepartmentStepDetailDto(
    val id: Int,
    val clearanceId: Int,
    val department_name: String,
    val full_name: String,
    val admission_number: String,
    val status: String,
    val remarks: String,
    val has_dues: Boolean,
    val dues_amount: Double,
    val records: List<DepartmentStepRecordDto>
)