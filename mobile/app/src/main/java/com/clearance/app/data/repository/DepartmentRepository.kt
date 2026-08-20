package com.clearance.app.data.repository

import com.clearance.app.data.api.ApiService
import com.clearance.app.data.api.RetrofitClient
import com.clearance.app.data.api.dto.CreateDepartmentRequest
import com.clearance.app.data.api.dto.DepartmentDto
import com.clearance.app.data.api.dto.MessageResponse
import com.clearance.app.data.api.dto.UpdateDepartmentRequest
import com.google.gson.JsonParser
import retrofit2.Response
import java.io.IOException

class DepartmentRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun getDepartments(): ClearanceResult<List<DepartmentDto>> =
        safeCall { apiService.getDepartments() }

    suspend fun createDepartment(request: CreateDepartmentRequest): ClearanceResult<MessageResponse> =
        safeCall { apiService.createDepartment(request) }

    suspend fun updateDepartment(id: Int, request: UpdateDepartmentRequest): ClearanceResult<MessageResponse> =
        safeCall { apiService.updateDepartment(id, request) }

    suspend fun deleteDepartment(id: Int): ClearanceResult<MessageResponse> =
        safeCall { apiService.deleteDepartment(id) }

    private suspend fun <T> safeCall(call: suspend () -> Response<T>): ClearanceResult<T> {
        return try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ClearanceResult.Success(body)
                } else {
                    ClearanceResult.Failure("Server returned an empty response.")
                }
            } else {
                val backendMessage = extractMessage(response.errorBody()?.string())
                ClearanceResult.Failure(backendMessage ?: "Request failed (HTTP ${response.code()}).")
            }
        } catch (e: IOException) {
            ClearanceResult.Failure(
                "Could not reach the server. Check that the backend is " +
                        "running and that your phone is on the same Wi-Fi network."
            )
        } catch (e: Exception) {
            ClearanceResult.Failure("Something went wrong. Please try again.")
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