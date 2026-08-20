package com.clearance.app.data.repository

import com.clearance.app.data.api.ApiService
import com.clearance.app.data.api.RetrofitClient
import com.clearance.app.data.api.dto.DepartmentDecisionRequest
import com.clearance.app.data.api.dto.DepartmentDecisionResponse
import com.clearance.app.data.api.dto.DepartmentQueueItemDto
import com.clearance.app.data.api.dto.DepartmentStepDetailDto
import com.google.gson.JsonParser
import retrofit2.Response
import java.io.IOException

/**
 * Handles the three HOD clearance-review endpoints. Named distinctly
 * from DepartmentRepository.kt (Milestone 5.3's admin department-CRUD
 * repository) to avoid conflating two different responsibilities that
 * only share the word "department" — that file is untouched.
 */
class DepartmentQueueRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun getQueue(): ClearanceResult<List<DepartmentQueueItemDto>> =
        safeCall { apiService.getDepartmentQueue() }

    suspend fun getStepDetail(stepId: Int): ClearanceResult<DepartmentStepDetailDto> =
        safeCall { apiService.getDepartmentStep(stepId) }

    suspend fun submitDecision(
        stepId: Int,
        status: String,
        remarks: String
    ): ClearanceResult<DepartmentDecisionResponse> =
        safeCall {
            apiService.submitDepartmentDecision(
                stepId,
                DepartmentDecisionRequest(
                    status = status,
                    remarks = remarks,
                    has_dues = false,
                    dues_amount = 0.0
                )
            )
        }

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