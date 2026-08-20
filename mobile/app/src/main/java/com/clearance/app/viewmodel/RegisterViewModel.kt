package com.clearance.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.api.dto.RegisterStudentRequest
import com.clearance.app.data.repository.AuthRepository
import com.clearance.app.data.repository.AuthResult
import kotlinx.coroutines.launch

data class RegisterUiState(
    val fullName: String = "",
    val admissionNumber: String = "",
    val kcseYear: String = "",
    val classForm: String = "",
    val stream: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistered: Boolean = false
)

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState by mutableStateOf(RegisterUiState())
        private set

    fun onFullNameChange(v: String) { uiState = uiState.copy(fullName = v, errorMessage = null) }
    fun onAdmissionNumberChange(v: String) { uiState = uiState.copy(admissionNumber = v, errorMessage = null) }
    fun onKcseYearChange(v: String) { uiState = uiState.copy(kcseYear = v, errorMessage = null) }
    fun onClassFormChange(v: String) { uiState = uiState.copy(classForm = v, errorMessage = null) }
    fun onStreamChange(v: String) { uiState = uiState.copy(stream = v, errorMessage = null) }
    fun onPhoneChange(v: String) { uiState = uiState.copy(phone = v, errorMessage = null) }
    fun onEmailChange(v: String) { uiState = uiState.copy(email = v, errorMessage = null) }
    fun onPasswordChange(v: String) { uiState = uiState.copy(password = v, errorMessage = null) }

    /**
     * Required-field check mirrors the HTML `required` attributes in
     * RegisterStudent.js exactly: full_name, admission_number, email,
     * password. kcse_year/class_form/stream/phone are optional there
     * too, so they are not validated here either.
     */
    fun register() {
        if (uiState.isLoading) return

        if (uiState.fullName.isBlank() ||
            uiState.admissionNumber.isBlank() ||
            uiState.email.isBlank() ||
            uiState.password.isBlank()
        ) {
            uiState = uiState.copy(errorMessage = "Please fill in all required fields (marked *).")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            val request = RegisterStudentRequest(
                full_name = uiState.fullName.trim(),
                admission_number = uiState.admissionNumber.trim(),
                kcse_year = uiState.kcseYear,
                class_form = uiState.classForm,
                stream = uiState.stream,
                phone = uiState.phone,
                email = uiState.email.trim(),
                password = uiState.password
            )

            when (val result = repository.registerStudent(request)) {
                is AuthResult.Success -> {
                    uiState = uiState.copy(isLoading = false, isRegistered = true)
                }
                is AuthResult.Failure -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}