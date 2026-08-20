package com.clearance.app.data.repository

import com.clearance.app.data.api.ApiService
import com.clearance.app.data.api.RetrofitClient
import com.clearance.app.data.api.dto.ClearanceDto
import com.clearance.app.data.api.dto.ClearanceStartRequest
import com.clearance.app.data.api.dto.ClearanceStartResponse
import com.clearance.app.data.api.dto.RecordDto
import com.clearance.app.data.api.dto.StudentMeDto
import com.google.gson.JsonParser
import retrofit2.Response
import java.io.IOException

sealed class ClearanceResult<out T> {
    data class Success<out T>(val data: T) : ClearanceResult<T>()
    data class Failure(val message: String) : ClearanceResult<Nothing>()
}

/**
 * Handles the four student-clearance endpoints confirmed in Milestone
 * 4.3's analysis. Follows AuthRepository's exact pattern — same
 * try/catch shape, same error-message extraction — kept as a
 * separate file (rather than added to AuthRepository) since these
 * are clearance/student endpoints, not auth endpoints.
 */
class ClearanceRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun getStudentMe(): ClearanceResult<StudentMeDto> =
        safeCall { apiService.getStudentMe() }

    suspend fun getMyLatestClearance(): ClearanceResult<ClearanceDto> =
        safeCall { apiService.getMyLatestClearance() }

    suspend fun startClearance(reason: String, reasonOther: String): ClearanceResult<ClearanceStartResponse> =
        safeCall { apiService.startClearance(ClearanceStartRequest(reason, reasonOther)) }

    suspend fun getMyRecords(): ClearanceResult<List<RecordDto>> =
        safeCall { apiService.getMyRecords() }

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