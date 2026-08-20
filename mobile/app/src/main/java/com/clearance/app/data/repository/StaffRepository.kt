package com.clearance.app.data.repository

import android.util.Log
import com.clearance.app.data.api.ApiService
import com.clearance.app.data.api.RetrofitClient
import com.clearance.app.data.api.dto.AdminUserDto
import com.clearance.app.data.api.dto.LogRecordRequest
import com.clearance.app.data.api.dto.MessageResponse
import com.clearance.app.data.api.dto.RecordDto
import com.clearance.app.data.api.dto.UserProfileDto
import com.google.gson.JsonParser
import retrofit2.Response
import java.io.IOException

private const val TAG = "StaffRepository"

class StaffRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun getMyProfile(): ClearanceResult<UserProfileDto> =
        safeCall("getMyProfile") { apiService.getUserProfile() }

    suspend fun getStudents(): ClearanceResult<List<AdminUserDto>> =
        safeCall("getStudents") { apiService.getStaffStudents() }

    suspend fun getMyLogs(): ClearanceResult<List<RecordDto>> =
        safeCall("getMyLogs") { apiService.getMyLogsForStaff() }

    suspend fun getStudentRecords(studentId: Int): ClearanceResult<List<RecordDto>> =
        safeCall("getStudentRecords") { apiService.getStudentRecordsForStaff(studentId) }

    suspend fun logRecord(studentId: Int, description: String, amount: Double?): ClearanceResult<MessageResponse> =
        safeCall("logRecord") { apiService.logRecordAsStaff(LogRecordRequest(studentId, description, amount)) }

    suspend fun resolveRecord(recordId: Int): ClearanceResult<MessageResponse> =
        safeCall("resolveRecord") { apiService.resolveRecordAsStaff(recordId) }

    private suspend fun <T> safeCall(label: String, call: suspend () -> Response<T>): ClearanceResult<T> {
        Log.d(TAG, "$label: starting request")
        return try {
            val response = call()
            Log.d(TAG, "$label: HTTP ${response.code()}, isSuccessful=${response.isSuccessful}")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d(TAG, "$label: success")
                    ClearanceResult.Success(body)
                } else {
                    Log.e(TAG, "$label: success but body is null")
                    ClearanceResult.Failure("Server returned an empty response.")
                }
            } else {
                val errorBodyString = response.errorBody()?.string()
                Log.e(TAG, "$label: HTTP ${response.code()} error body: $errorBodyString")
                val backendMessage = extractMessage(errorBodyString)
                ClearanceResult.Failure(backendMessage ?: "Request failed (HTTP ${response.code()}).")
            }
        } catch (e: IOException) {
            Log.e(TAG, "$label: IOException - ${e.message}", e)
            ClearanceResult.Failure(
                "Could not reach the server. Check that the backend is " +
                        "running and that your phone is on the same Wi-Fi network."
            )
        } catch (e: Exception) {
            Log.e(TAG, "$label: Exception - ${e::class.simpleName}: ${e.message}", e)
            ClearanceResult.Failure("Something went wrong. Please try again.")
        }
    }

    private fun extractMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val json = JsonParser.parseString(errorBody).asJsonObject
            when {
                json.has("message") -> json.get("message").asString
                json.has("error") -> json.get("error").asString
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}