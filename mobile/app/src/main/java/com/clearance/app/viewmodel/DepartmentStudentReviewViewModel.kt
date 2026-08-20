package com.clearance.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.api.dto.DepartmentStepDetailDto
import com.clearance.app.data.repository.ClearanceResult
import com.clearance.app.data.repository.DepartmentQueueRepository
import kotlinx.coroutines.launch

data class DepartmentReviewUiState(
    val isLoading: Boolean = true,
    val loadError: String? = null,
    val detail: DepartmentStepDetailDto? = null,
    val remarks: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val decisionSuccessMessage: String? = null
)

/**
 * Mirrors client/src/pages/department/DeptStudentReview.js. load()
 * is called once from the screen (via LaunchedEffect(stepId)) rather
 * than from init{}, since this ViewModel needs the stepId that only
 * the nav-graph route argument provides — consistent with this
 * project's existing pattern of keeping ViewModels constructible with
 * no arguments via the default viewModel() factory.
 */
class DepartmentStudentReviewViewModel(
    private val repository: DepartmentQueueRepository = DepartmentQueueRepository()
) : ViewModel() {

    var uiState by mutableStateOf(DepartmentReviewUiState())
        private set

    private var loadedStepId: Int? = null

    fun load(stepId: Int) {
        if (loadedStepId == stepId) return
        loadedStepId = stepId

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, loadError = null)
            when (val result = repository.getStepDetail(stepId)) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(
                        isLoading = false,
                        detail = result.data,
                        remarks = result.data.remarks
                    )
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(isLoading = false, loadError = result.message)
                }
            }
        }
    }

    fun onRemarksChange(value: String) {
        uiState = uiState.copy(remarks = value)
    }

    /**
     * status must be exactly "cleared", "rejected", or "pending" —
     * matching the three real action buttons in DeptStudentReview.js.
     * The web app's status <select> is functionally dead (its value
     * is never actually sent — every save() call there passes its
     * own literal status), so this ViewModel doesn't model a
     * dropdown-driven submit either.
     */
    fun submitDecision(stepId: Int, status: String) {
        if (uiState.isSubmitting) return
        viewModelScope.launch {
            uiState = uiState.copy(isSubmitting = true, submitError = null)
            when (val result = repository.submitDecision(stepId, status, uiState.remarks)) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(isSubmitting = false, decisionSuccessMessage = result.data.message)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(isSubmitting = false, submitError = result.message)
                }
            }
        }
    }

    fun consumeSuccess() {
        uiState = uiState.copy(decisionSuccessMessage = null)
    }
}