package com.clearance.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.api.dto.AdminUserDto
import com.clearance.app.data.api.dto.CreateUserRequest
import com.clearance.app.data.api.dto.DepartmentDto
import com.clearance.app.data.api.dto.UpdateUserRequest
import com.clearance.app.data.repository.AdminRepository
import com.clearance.app.data.repository.ClearanceResult
import kotlinx.coroutines.launch

data class UserFormState(
    val fullName: String = "",
    val email: String = "",
    val username: String = "",
    val role: String = "hod",
    val department: String = "",
    val password: String = "",
    val editingId: Int? = null
)

data class AdminUsersUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val users: List<AdminUserDto> = emptyList(),
    val departments: List<DepartmentDto> = emptyList(),
    val formState: UserFormState = UserFormState(),
    val isSavingForm: Boolean = false,
    val formError: String? = null,
    val mutatingUserId: Int? = null,
    val deletingUser: AdminUserDto? = null,
    val isDeleting: Boolean = false,
    val deactivatingUser: AdminUserDto? = null,
    val isDeactivating: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

/**
 * Mirrors client/src/pages/admin/ManageUsers.js: loads users +
 * departments together, offers create/edit (single shared form, per
 * the web page), activate (reused endpoint), deactivate (with
 * confirmation, matching the web's window.confirm), delete (with
 * confirmation).
 */
class AdminUsersViewModel(
    private val repository: AdminRepository = AdminRepository()
) : ViewModel() {

    var uiState by mutableStateOf(AdminUsersUiState())
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

            val usersResult = repository.getUsers()
            val departmentsResult = repository.getDepartments()

            val newUsers = when (usersResult) {
                is ClearanceResult.Success -> usersResult.data
                is ClearanceResult.Failure -> emptyList()
            }
            val newDepartments = when (departmentsResult) {
                is ClearanceResult.Success -> departmentsResult.data
                is ClearanceResult.Failure -> emptyList()
            }

            val loadErrorMessage = (usersResult as? ClearanceResult.Failure)?.message

            uiState = uiState.copy(
                isLoading = false,
                isRefreshing = false,
                users = newUsers,
                departments = newDepartments,
                errorMessage = loadErrorMessage
            )
        }
    }

    fun onFullNameChange(v: String) { uiState = uiState.copy(formState = uiState.formState.copy(fullName = v)) }
    fun onEmailChange(v: String) { uiState = uiState.copy(formState = uiState.formState.copy(email = v)) }
    fun onUsernameChange(v: String) { uiState = uiState.copy(formState = uiState.formState.copy(username = v)) }
    fun onRoleChange(v: String) { uiState = uiState.copy(formState = uiState.formState.copy(role = v)) }
    fun onDepartmentChange(v: String) { uiState = uiState.copy(formState = uiState.formState.copy(department = v)) }
    fun onPasswordChange(v: String) { uiState = uiState.copy(formState = uiState.formState.copy(password = v)) }

    fun startEdit(user: AdminUserDto) {
        uiState = uiState.copy(
            formState = UserFormState(
                fullName = user.full_name,
                email = user.email,
                username = user.username,
                role = user.role,
                department = user.department,
                password = "",
                editingId = user.id
            ),
            formError = null
        )
    }

    fun cancelEdit() {
        uiState = uiState.copy(formState = UserFormState(), formError = null)
    }

    fun submitForm() {
        val form = uiState.formState
        if (form.fullName.isBlank() || form.email.isBlank() || form.username.isBlank()) {
            uiState = uiState.copy(formError = "Full name, email, and username are required.")
            return
        }
        if (form.editingId == null && form.password.isBlank()) {
            uiState = uiState.copy(formError = "Password is required for a new user.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isSavingForm = true, formError = null)

            val result = if (form.editingId != null) {
                repository.updateUser(
                    form.editingId,
                    UpdateUserRequest(
                        full_name = form.fullName.trim(),
                        email = form.email.trim(),
                        username = form.username.trim(),
                        role = form.role,
                        department = form.department,
                        password = form.password
                    )
                )
            } else {
                repository.createUser(
                    CreateUserRequest(
                        full_name = form.fullName.trim(),
                        email = form.email.trim(),
                        username = form.username.trim(),
                        role = form.role,
                        department = form.department,
                        password = form.password
                    )
                )
            }

            when (result) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(
                        isSavingForm = false,
                        formState = UserFormState(),
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

    fun activateUser(userId: Int) {
        if (uiState.mutatingUserId != null) return
        viewModelScope.launch {
            uiState = uiState.copy(mutatingUserId = userId)
            when (val result = repository.activateUser(userId)) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(mutatingUserId = null, successMessage = result.data.message)
                    load(initial = false)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(mutatingUserId = null, errorMessage = result.message)
                }
            }
        }
    }

    fun requestDeactivate(user: AdminUserDto) {
        uiState = uiState.copy(deactivatingUser = user)
    }

    fun dismissDeactivateDialog() {
        if (uiState.isDeactivating) return
        uiState = uiState.copy(deactivatingUser = null)
    }

    fun confirmDeactivate() {
        val target = uiState.deactivatingUser ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isDeactivating = true)
            when (val result = repository.deactivateUser(target.id)) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(
                        isDeactivating = false,
                        deactivatingUser = null,
                        successMessage = result.data.message
                    )
                    load(initial = false)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(isDeactivating = false, deactivatingUser = null, errorMessage = result.message)
                }
            }
        }
    }

    fun requestDelete(user: AdminUserDto) {
        uiState = uiState.copy(deletingUser = user)
    }

    fun dismissDeleteDialog() {
        if (uiState.isDeleting) return
        uiState = uiState.copy(deletingUser = null)
    }

    fun confirmDelete() {
        val target = uiState.deletingUser ?: return
        viewModelScope.launch {
            uiState = uiState.copy(isDeleting = true)
            when (val result = repository.deleteUser(target.id)) {
                is ClearanceResult.Success -> {
                    uiState = uiState.copy(
                        isDeleting = false,
                        deletingUser = null,
                        successMessage = result.data.message
                    )
                    load(initial = false)
                }
                is ClearanceResult.Failure -> {
                    uiState = uiState.copy(isDeleting = false, deletingUser = null, errorMessage = result.message)
                }
            }
        }
    }

    fun consumeMessage() {
        uiState = uiState.copy(successMessage = null, errorMessage = null)
    }
}