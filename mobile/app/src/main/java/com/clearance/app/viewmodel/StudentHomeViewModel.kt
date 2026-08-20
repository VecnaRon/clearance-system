package com.clearance.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.api.dto.ClearanceDto
import com.clearance.app.data.repository.ClearanceRepository
import com.clearance.app.data.repository.ClearanceResult
import kotlinx.coroutines.launch

/**
 * Real, backend-confirmed clearance states. NOTE: there is no
 * "REJECTED" clearance-level status in the current backend — see the
 * findings note in this milestone's response. A rejected step keeps
 * the parent clearance at PENDING.
 */
enum class ClearanceLifecycleState { NO_CLEARANCE, PENDING, AWAITING_FINAL, APPROVED, UNKNOWN }

fun ClearanceDto?.toLifecycleState(): ClearanceLifecycleState {
    if (this == null || this.id == null) return ClearanceLifecycleState.NO_CLEARANCE
    return when (this.status) {
        "pending" -> ClearanceLifecycleState.PENDING
        "awaiting_final" -> ClearanceLifecycleState.AWAITING_FINAL
        "approved" -> ClearanceLifecycleState.APPROVED
        else -> ClearanceLifecycleState.UNKNOWN
    }
}

data class StudentHomeUiState(
    val isLoading: Boolean = true,
    val clearance: ClearanceDto? = null,
    val loadError: String? = null
)

/**
 * Reuses the existing ClearanceRepository.getMyLatestClearance() —
 * no new endpoint. Backs the state-aware StudentStartScreen (Issue #2/#3).
 */
class StudentHomeViewModel(
    private val repository: ClearanceRepository = ClearanceRepository()
) : ViewModel() {

    var uiState by mutableStateOf(StudentHomeUiState())
        private set

    init { load() }

    fun refresh() = load()

    private fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, loadError = null)
            when (val result = repository.getMyLatestClearance()) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(isLoading = false, clearance = result.data)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(isLoading = false, loadError = result.message)
                }
            }
        }
    }
}