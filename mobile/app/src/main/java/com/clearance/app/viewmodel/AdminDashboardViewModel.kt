package com.clearance.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.api.dto.AdminOverviewDto
import com.clearance.app.data.api.dto.AuditLogDto
import com.clearance.app.data.api.dto.PendingUserDto
import com.clearance.app.data.repository.AdminRepository
import com.clearance.app.data.repository.ClearanceResult
import kotlinx.coroutines.launch

data class AdminDashboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val overview: List<AdminOverviewDto> = emptyList(),
    val pendingUsers: List<PendingUserDto> = emptyList(),
    val auditLogs: List<AuditLogDto> = emptyList(),
    val activatingUserId: Int? = null,
    val approvingClearanceId: Int? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class AdminDashboardViewModel(
    private val repository: AdminRepository = AdminRepository()
) : ViewModel() {

    var uiState by mutableStateOf(AdminDashboardUiState())
        private set

    init {
        loadAll(initial = true)
    }

    fun refresh() {
        if (uiState.isRefreshing) return
        loadAll(initial = false)
    }

    private fun loadAll(initial: Boolean) {
        viewModelScope.launch {
            uiState = if (initial) {
                uiState.copy(isLoading = true)
            } else {
                uiState.copy(isRefreshing = true)
            }

            val overviewResult = repository.getOverview()
            val pendingUsersResult = repository.getPendingUsers()
            val auditLogsResult = repository.getAuditLogs()

            val newOverview = when (overviewResult) {
                is ClearanceResult.Success -> overviewResult.data
                is ClearanceResult.Failure -> emptyList()
            }
            val newPendingUsers = when (pendingUsersResult) {
                is ClearanceResult.Success -> pendingUsersResult.data
                is ClearanceResult.Failure -> emptyList()
            }
            val newAuditLogs = when (auditLogsResult) {
                is ClearanceResult.Success -> auditLogsResult.data
                is ClearanceResult.Failure -> emptyList()
            }

            uiState = uiState.copy(
                isLoading = false,
                isRefreshing = false,
                overview = newOverview,
                pendingUsers = newPendingUsers,
                auditLogs = newAuditLogs
            )
        }
    }

    fun activateUser(userId: Int) {
        if (uiState.activatingUserId != null) return
        viewModelScope.launch {
            uiState = uiState.copy(activatingUserId = userId, errorMessage = null, successMessage = null)
            when (val result = repository.activateUser(userId)) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(activatingUserId = null, successMessage = result.data.message)
                    loadAll(initial = false)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(activatingUserId = null, errorMessage = result.message)
                }
            }
        }
    }

    fun finalApprove(clearanceId: Int) {
        if (uiState.approvingClearanceId != null) return
        viewModelScope.launch {
            uiState = uiState.copy(approvingClearanceId = clearanceId, errorMessage = null, successMessage = null)
            when (val result = repository.finalApprove(clearanceId)) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(approvingClearanceId = null, successMessage = result.data.message)
                    loadAll(initial = false)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(approvingClearanceId = null, errorMessage = result.message)
                }
            }
        }
    }

    fun consumeMessage() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }
}