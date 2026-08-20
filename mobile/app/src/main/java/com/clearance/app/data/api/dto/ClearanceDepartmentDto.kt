package com.clearance.app.data.api.dto

/**
 * One entry in ClearanceDto.departments[].
 *
 * Matches services/clearanceService.js's getLatestForStudent mapping
 * exactly — these five fields are always present with these literal
 * types whenever a clearance exists:
 *   { id, department_name, status, remarks, has_dues: false, dues_amount: 0 }
 *
 * has_dues/dues_amount are always false/0 as returned by the current
 * backend (confirmed hardcoded literals, not derived from any table),
 * kept here for contract parity, not because they carry real data.
 */
data class ClearanceDepartmentDto(
    val id: Int,
    val department_name: String,
    val status: String,
    val remarks: String,
    val has_dues: Boolean,
    val dues_amount: Double
)