package com.clearance.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.api.dto.AdminUserDto
import com.clearance.app.data.api.dto.RecordDto
import com.clearance.app.data.api.dto.UserProfileDto
import com.clearance.app.data.repository.ClearanceResult
import com.clearance.app.data.repository.StaffRepository
import kotlinx.coroutines.launch

data class LogFeedback(val success: Boolean, val text: String)

data class StaffDashboardUiState(
    val me: UserProfileDto? = null,
    val students: List<AdminUserDto> = emptyList(),
    val searchQuery: String = "",
    val selectedStudent: AdminUserDto? = null,
    val studentRecords: List<RecordDto> = emptyList(),
    val loadingRecords: Boolean = false,
    val description: String = "",
    val amount: String = "",
    val submitting: Boolean = false,
    val logMessage: LogFeedback? = null,
    val myLogs: List<RecordDto> = emptyList(),
    val logsLoading: Boolean = true,
    val filterStatus: String = "all",
    val resolvingId: Int? = null
)

/**
 * Mirrors client/src/pages/staff/StaffDashboard.js's state machine
 * exactly — separate loading flags per concern (loadingRecords,
 * submitting, logsLoading, resolvingId), not one shared boolean.
 */
class StaffDashboardViewModel(
    private val repository: StaffRepository = StaffRepository()
) : ViewModel() {

    var uiState by mutableStateOf(StaffDashboardUiState())
        private set

    init {
        loadProfile()
        loadStudents()
        loadMyLogs()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            when (val result = repository.getMyProfile()) {
                is ClearanceResult.Success -> uiState = uiState.copy(me = result.data)
                is ClearanceResult.Failure -> { /* matches web: console.error only, profile stays null */ }
            }
        }
    }

    private fun loadStudents() {
        viewModelScope.launch {
            when (val result = repository.getStudents()) {
                is ClearanceResult.Success -> uiState = uiState.copy(students = result.data)
                is ClearanceResult.Failure -> uiState = uiState.copy(students = emptyList())
            }
        }
    }

    fun loadMyLogs() {
        viewModelScope.launch {
            uiState = uiState.copy(logsLoading = true)
            when (val result = repository.getMyLogs()) {
                is ClearanceResult.Success -> uiState = uiState.copy(logsLoading = false, myLogs = result.data)
                is ClearanceResult.Failure -> uiState = uiState.copy(logsLoading = false, myLogs = emptyList())
            }
        }
    }

    private fun loadStudentRecords(studentId: Int) {
        viewModelScope.launch {
            uiState = uiState.copy(loadingRecords = true)
            when (val result = repository.getStudentRecords(studentId)) {
                is ClearanceResult.Success -> uiState = uiState.copy(loadingRecords = false, studentRecords = result.data)
                is ClearanceResult.Failure -> uiState = uiState.copy(loadingRecords = false, studentRecords = emptyList())
            }
        }
    }

    fun onSearchQueryChange(value: String) {
        uiState = uiState.copy(searchQuery = value)
    }

    fun selectStudent(student: AdminUserDto) {
        uiState = uiState.copy(
            selectedStudent = student,
            description = "",
            amount = "",
            logMessage = null
        )
        loadStudentRecords(student.id)
    }

    fun closeSelectedStudent() {
        uiState = uiState.copy(selectedStudent = null, studentRecords = emptyList(), description = "", amount = "")
    }

    fun onDescriptionChange(value: String) {
        uiState = uiState.copy(description = value)
    }

    fun onAmountChange(value: String) {
        uiState = uiState.copy(amount = value)
    }

    fun logRecord() {
        val student = uiState.selectedStudent ?: return
        if (uiState.description.isBlank()) return

        viewModelScope.launch {
            uiState = uiState.copy(submitting = true, logMessage = null)
            val amountValue = uiState.amount.trim().toDoubleOrNull()
            when (val result = repository.logRecord(student.id, uiState.description.trim(), amountValue)) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(
                        submitting = false,
                        logMessage = LogFeedback(true, "Record logged successfully for ${student.full_name}."),
                        description = "",
                        amount = ""
                    )
                    loadMyLogs()
                    loadStudentRecords(student.id)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(submitting = false, logMessage = LogFeedback(false, result.message))
                }
            }
        }
    }

    fun resolveRecord(recordId: Int) {
        if (uiState.resolvingId != null) return
        viewModelScope.launch {
            uiState = uiState.copy(resolvingId = recordId)
            when (repository.resolveRecord(recordId)) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(resolvingId = null)
                    loadMyLogs()
                    uiState.selectedStudent?.let { loadStudentRecords(it.id) }
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(resolvingId = null)
                }
            }
        }
    }

    fun onFilterStatusChange(value: String) {
        uiState = uiState.copy(filterStatus = value)
    }
}