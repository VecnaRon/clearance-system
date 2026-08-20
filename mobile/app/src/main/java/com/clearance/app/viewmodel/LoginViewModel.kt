package com.clearance.app.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.api.dto.UserDto
import com.clearance.app.data.repository.AuthRepository
import com.clearance.app.data.repository.AuthResult
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: UserDto? = null
)

class LoginViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(value: String) {
        uiState = uiState.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        uiState = uiState.copy(password = value, errorMessage = null)
    }

    fun login() {
        if (uiState.isLoading) return

        val email = uiState.email.trim()
        val password = uiState.password

        if (email.isEmpty() || password.isEmpty()) {
            uiState = uiState.copy(errorMessage = "Please enter both email and password.")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)

            when (val result = repository.login(email, password)) {
                is AuthResult.Success -> {
                    uiState = uiState.copy(isLoading = false, loggedInUser = result.user)
                }
                is AuthResult.Failure -> {
                    uiState = uiState.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
