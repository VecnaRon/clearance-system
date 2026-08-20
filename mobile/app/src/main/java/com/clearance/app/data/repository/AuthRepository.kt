package com.clearance.app.data.repository

import com.clearance.app.data.api.ApiService
import com.clearance.app.data.api.RetrofitClient
import com.clearance.app.data.api.dto.LoginRequest
import com.clearance.app.data.api.dto.RegisterStudentRequest
import com.clearance.app.data.api.dto.UserDto
import com.clearance.app.data.local.SessionManager
import com.google.gson.JsonParser
import java.io.IOException

sealed class AuthResult {
    data class Success(val token: String, val user: UserDto) : AuthResult()
    data class Failure(val message: String) : AuthResult()
}

/**
 * Talks to POST /api/auth/login and POST /api/auth/register-student.
 */
class AuthRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = apiService.login(LoginRequest(email, password))

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    SessionManager.saveToken(body.token)
                    SessionManager.saveUser(body.user)
                    AuthResult.Success(body.token, body.user)
                } else {
                    AuthResult.Failure("Server returned an empty response.")
                }
            } else {
                val backendMessage = extractMessage(response.errorBody()?.string())
                val fallback = when (response.code()) {
                    400 -> "Invalid credentials"
                    403 -> "Account is inactive. Contact the administrator."
                    else -> "Login failed (HTTP ${response.code()})."
                }
                AuthResult.Failure(backendMessage ?: fallback)
            }
        } catch (e: IOException) {
            AuthResult.Failure(
                "Could not reach the server. Check that the backend is " +
                        "running and that your phone is on the same Wi-Fi network."
            )
        } catch (e: Exception) {
            AuthResult.Failure("Something went wrong. Please try again.")
        }
    }

    /**
     * STAGE 4.1 addition.
     *
     * Deliberately does NOT call SessionManager.saveToken/saveUser on
     * success. The real web app (client/src/pages/auth/RegisterStudent.js)
     * does not auto-login after registration either — it shows a
     * "pending activation" message and sends the user back to Login.
     * New accounts are created with is_active = 0 server-side (see
     * auth.controller.js's registerStudent), so the returned token
     * would belong to an inactive account anyway. This function still
     * returns the token/user in AuthResult.Success in case a future
     * screen needs to inspect them, but the caller (RegisterViewModel)
     * does not act on them beyond showing the success state.
     */
    suspend fun registerStudent(request: RegisterStudentRequest): AuthResult {
        return try {
            val response = apiService.registerStudent(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    AuthResult.Success(body.token, body.user)
                } else {
                    AuthResult.Failure("Server returned an empty response.")
                }
            } else {
                val backendMessage = extractMessage(response.errorBody()?.string())
                val fallback = when (response.code()) {
                    400 -> "Email or Admission Number already exists"
                    500 -> "Registration failed"
                    else -> "Registration failed (HTTP ${response.code()})."
                }
                AuthResult.Failure(backendMessage ?: fallback)
            }
        } catch (e: IOException) {
            AuthResult.Failure(
                "Could not reach the server. Check that the backend is " +
                        "running and that your phone is on the same Wi-Fi network."
            )
        } catch (e: Exception) {
            AuthResult.Failure("Something went wrong. Please try again.")
        }
    }

    private fun extractMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val json = JsonParser.parseString(errorBody).asJsonObject
            if (json.has("message")) json.get("message").asString else null
        } catch (e: Exception) {
            null
        }
    }
}