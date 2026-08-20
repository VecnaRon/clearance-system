package com.clearance.app.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.api.dto.AuditLogDto
import com.clearance.app.data.repository.AdminRepository
import com.clearance.app.data.repository.ClearanceResult
import kotlinx.coroutines.launch

private const val TAG = "AdminAuditViewModel"

data class AdminAuditUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val logs: List<AuditLogDto> = emptyList(),
    val errorMessage: String? = null
)

class AdminAuditViewModel(
    private val repository: AdminRepository = AdminRepository()
) : ViewModel() {

    var uiState by mutableStateOf(AdminAuditUiState())
        private set

    init {
        Log.d(TAG, "init: triggering first load")
        load(initial = true)
    }

    fun refresh() {
        if (uiState.isRefreshing) return
        Log.d(TAG, "refresh() called")
        load(initial = false)
    }

    private fun load(initial: Boolean) {
        viewModelScope.launch {
            uiState = if (initial) uiState.copy(isLoading = true) else uiState.copy(isRefreshing = true)
            Log.d(TAG, "load: isLoading=${uiState.isLoading} isRefreshing=${uiState.isRefreshing}")

            when (val result = repository.getAuditLogs()) {
                is ClearanceResult.Success -> {
                    Log.d(TAG, "load: SUCCESS, ${result.data.size} logs received")
                    uiState = uiState.copy(isLoading = false, isRefreshing = false, logs = result.data, errorMessage = null)
                }
                is ClearanceResult.Failure -> {
                    Log.e(TAG, "load: FAILURE - ${result.message}")
                    uiState = uiState.copy(isLoading = false, isRefreshing = false, errorMessage = result.message)
                }
            }
            Log.d(TAG, "load: final state isLoading=${uiState.isLoading}")
        }
    }
}