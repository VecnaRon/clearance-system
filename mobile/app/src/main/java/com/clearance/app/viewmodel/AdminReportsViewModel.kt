package com.clearance.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.api.dto.DepartmentReportDto
import com.clearance.app.data.repository.AdminRepository
import com.clearance.app.data.repository.ClearanceResult
import kotlinx.coroutines.launch

data class AdminReportsUiState(
    val isLoading: Boolean = true,
    val rows: List<DepartmentReportDto> = emptyList(),
    val errorMessage: String? = null
)

/** Mirrors client/src/pages/admin/Reports.js — single load, no refresh in the web page either. */
class AdminReportsViewModel(
    private val repository: AdminRepository = AdminRepository()
) : ViewModel() {

    var uiState by mutableStateOf(AdminReportsUiState())
        private set

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            when (val result = repository.getClearanceReport()) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(isLoading = false, rows = result.data, errorMessage = null)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}