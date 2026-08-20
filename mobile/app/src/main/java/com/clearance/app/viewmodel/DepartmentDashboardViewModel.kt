package com.clearance.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.api.dto.DepartmentQueueItemDto
import com.clearance.app.data.repository.ClearanceResult
import com.clearance.app.data.repository.DepartmentQueueRepository
import kotlinx.coroutines.launch

data class DepartmentDashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val queue: List<DepartmentQueueItemDto> = emptyList(),
    val errorMessage: String? = null
)

/**
 * Mirrors client/src/pages/department/DeptDashboard.js: loads the
 * HOD's own-department queue, computes total/pending/cleared/rejected
 * counts client-side exactly as the web page does ("cleared" = count
 * of backend status "approved").
 */
class DepartmentDashboardViewModel(
    private val repository: DepartmentQueueRepository = DepartmentQueueRepository()
) : ViewModel() {

    var uiState by mutableStateOf(DepartmentDashboardUiState())
        private set

    init {
        loadQueue(initial = true)
    }

    fun refresh() {
        if (uiState.isRefreshing) return
        loadQueue(initial = false)
    }

    private fun loadQueue(initial: Boolean) {
        viewModelScope.launch {
            uiState = if (initial) uiState.copy(isLoading = true) else uiState.copy(isRefreshing = true)
            when (val result = repository.getQueue()) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(isLoading = false, isRefreshing = false, queue = result.data, errorMessage = null)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(isLoading = false, isRefreshing = false, errorMessage = result.message)
                }
            }
        }
    }
}