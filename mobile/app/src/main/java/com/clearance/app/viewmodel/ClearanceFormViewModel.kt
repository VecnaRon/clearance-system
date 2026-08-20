package com.clearance.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.repository.ClearanceRepository
import com.clearance.app.data.repository.ClearanceResult
import kotlinx.coroutines.launch

data class ClearanceFormUiState(
    val isLoadingInitial: Boolean = true,
    val loadError: String? = null,
    val fullName: String = "",
    val admissionNumber: String = "",
    val hasActiveClearance: Boolean = false,
    val reason: String = "kcse_completion",
    val reasonOther: String = "",
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submittedClearanceId: Int? = null
)

/**
 * Mirrors client/src/pages/student/ClearanceForm.js exactly:
 *   1. On load: GET /student/me, then GET /clearance/my-latest to
 *      check for an existing active clearance.
 *   2. If active: show the "already have an active clearance" state.
 *   3. Else: show the real form (reason select + conditional "other").
 *   4. On submit: POST /clearance/start, then (matching the web app's
 *      2-second delay before redirecting) hold the success state
 *      briefly before the screen navigates to Clearance Status.
 */
class ClearanceFormViewModel(
    private val repository: ClearanceRepository = ClearanceRepository()
) : ViewModel() {

    var uiState by mutableStateOf(ClearanceFormUiState())
        private set

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingInitial = true, loadError = null)

            when (val meResult = repository.getStudentMe()) {
                is ClearanceResult.Success -> {
                    val me = meResult.data
                    uiState = uiState.copy(
                        fullName = me.full_name ?: "",
                        admissionNumber = me.admission_number ?: ""
                    )

                    when (val latestResult = repository.getMyLatestClearance()) {
                        is ClearanceResult.Success -> {
                            uiState = uiState.copy(
                                isLoadingInitial = false,
                                hasActiveClearance = latestResult.data.id != null
                            )
                        }
                        is ClearanceResult.Failure -> {
                            // Matches ClearanceForm.js: a failed my-latest call
                            // is silently swallowed and the form is shown
                            // as if there is no active clearance.
                            uiState = uiState.copy(
                                isLoadingInitial = false,
                                hasActiveClearance = false
                            )
                        }
                    }
                }
                is ClearanceResult.Failure -> {
                    // The real web page has no recovery here either — if
                    // /student/me fails, `me` stays null and the page is
                    // stuck on "Loading...". Surfacing the actual error
                    // message here (rather than an endless spinner) is the
                    // one deliberate improvement over that dead end.
                    uiState = uiState.copy(isLoadingInitial = false, loadError = meResult.message)
                }
            }
        }
    }

    fun onReasonChange(value: String) {
        uiState = uiState.copy(reason = value)
    }

    fun onReasonOtherChange(value: String) {
        uiState = uiState.copy(reasonOther = value)
    }

    fun submit() {
        if (uiState.isSubmitting) return
        viewModelScope.launch {
            uiState = uiState.copy(isSubmitting = true, submitError = null)
            when (val result = repository.startClearance(uiState.reason, uiState.reasonOther)) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(isSubmitting = false, submittedClearanceId = result.data.id)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(isSubmitting = false, submitError = result.message)
                }
            }
        }
    }
}