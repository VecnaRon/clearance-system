package com.clearance.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.api.dto.CreateDepartmentRequest
import com.clearance.app.data.api.dto.DepartmentDto
import com.clearance.app.data.api.dto.UpdateDepartmentRequest
import com.clearance.app.data.repository.ClearanceResult
import com.clearance.app.data.repository.DepartmentRepository
import kotlinx.coroutines.launch

data class DepartmentFormState(
    val name: String = "",
    val code: String = "",
    val description: String = "",
    val editingId: Int? = null
)

data class DepartmentsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val departments: List<DepartmentDto> = emptyList(),
    val showFormDialog: Boolean = false,
    val formState: DepartmentFormState = DepartmentFormState(),
    val isSavingForm: Boolean = false,
    val formError: String? = null,
    val deletingDepartment: DepartmentDto? = null,
    val isDeleting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * MILESTONE 5.3. Create form includes name/code/description (POST);
 * edit form includes only name/description, matching the confirmed
 * backend contract where PUT /admin/departments/:id ignores code and
 * is_active even if sent.
 */
class DepartmentViewModel(
    private val repository: DepartmentRepository = DepartmentRepository()
) : ViewModel() {

    var uiState by mutableStateOf(DepartmentsUiState())
        private set

    init {
        load(initial = true)
    }

    fun refresh() {
        if (uiState.isRefreshing) return
        load(initial = false)
    }

    private fun load(initial: Boolean) {
        viewModelScope.launch {
            uiState = if (initial) uiState.copy(isLoading = true) else uiState.copy(isRefreshing = true)
            when (val result = repository.getDepartments()) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(isLoading = false, isRefreshing = false, departments = result.data)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(isLoading = false, isRefreshing = false, errorMessage = result.message)
                }
            }
        }
    }

    fun openCreateDialog() {
        uiState = uiState.copy(
            showFormDialog = true,
            formState = DepartmentFormState(),
            formError = null
        )
    }

    fun openEditDialog(department: DepartmentDto) {
        uiState = uiState.copy(
            showFormDialog = true,
            formState = DepartmentFormState(
                name = department.name,
                code = department.code ?: "",
                description = department.description ?: "",
                editingId = department.id
            ),
            formError = null
        )
    }

    fun dismissFormDialog() {
        if (uiState.isSavingForm) return
        uiState = uiState.copy(showFormDialog = false, formError = null)
    }

    fun onFormNameChange(value: String) {
        uiState = uiState.copy(formState = uiState.formState.copy(name = value))
    }

    fun onFormCodeChange(value: String) {
        uiState = uiState.copy(formState = uiState.formState.copy(code = value))
    }

    fun onFormDescriptionChange(value: String) {
        uiState = uiState.copy(formState = uiState.formState.copy(description = value))
    }

    fun submitForm() {
        val form = uiState.formState
        if (form.name.isBlank()) {
            uiState = uiState.copy(formError = "Department name is required.")
            return
        }
        if (form.editingId == null && form.code.isBlank()) {
            uiState = uiState.copy(formError = "Department code is required.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSavingForm = true, formError = null)

            val result = if (form.editingId != null) {
                repository.updateDepartment(
                    form.editingId,
                    UpdateDepartmentRequest(name = form.name.trim(), description = form.description.trim())
                )
            } else {
                repository.createDepartment(
                    CreateDepartmentRequest(
                        name = form.name.trim(),
                        code = form.code.trim(),
                        description = form.description.trim()
                    )
                )
            }

            when (result) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(
                        isSavingForm = false,
                        showFormDialog = false,
                        successMessage = result.data.message
                    )
                    load(initial = false)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(isSavingForm = false, formError = result.message)
                }
            }
        }
    }

    fun requestDelete(department: DepartmentDto) {
        uiState = uiState.copy(deletingDepartment = department)
    }

    fun dismissDeleteDialog() {
        if (uiState.isDeleting) return
        uiState = uiState.copy(deletingDepartment = null)
    }

    fun confirmDelete() {
        val target = uiState.deletingDepartment ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isDeleting = true)
            when (val result = repository.deleteDepartment(target.id)) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(
                        isDeleting = false,
                        deletingDepartment = null,
                        successMessage = result.data.message
                    )
                    load(initial = false)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(isDeleting = false, errorMessage = result.message, deletingDepartment = null)
                }
            }
        }
    }

    fun consumeMessage() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }
}