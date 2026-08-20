package com.clearance.app.data.repository

import android.util.Log
import com.clearance.app.data.api.ApiService
import com.clearance.app.data.api.RetrofitClient
import com.clearance.app.data.api.dto.AdminOverviewDto
import com.clearance.app.data.api.dto.AdminUserDto
import com.clearance.app.data.api.dto.AuditLogDto
import com.clearance.app.data.api.dto.CreateUserRequest
import com.clearance.app.data.api.dto.DepartmentDto
import com.clearance.app.data.api.dto.DepartmentReportDto
import com.clearance.app.data.api.dto.MessageResponse
import com.clearance.app.data.api.dto.PendingUserDto
import com.clearance.app.data.api.dto.UpdateUserRequest
import com.google.gson.JsonParser
import retrofit2.Response
import java.io.IOException

private const val TAG = "AdminRepository"

class AdminRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun getOverview(): ClearanceResult<List<AdminOverviewDto>> =
        safeCall("getOverview") {
            apiService.getAdminOverview()
        }

    suspend fun getPendingUsers(): ClearanceResult<List<PendingUserDto>> =
        safeCall("getPendingUsers") {
            apiService.getPendingUsers()
        }

    suspend fun getAuditLogs(): ClearanceResult<List<AuditLogDto>> =
        safeCall("getAuditLogs") {
            apiService.getAuditLogs()
        }

    suspend fun getUsers(): ClearanceResult<List<AdminUserDto>> =
        safeCall("getUsers") {
            apiService.getUsers()
        }

    /**
     * Loads departments used by AdminUsersViewModel
     * for the create/edit user department dropdown.
     */
    suspend fun getDepartments(): ClearanceResult<List<DepartmentDto>> =
        safeCall("getDepartments") {
            apiService.getDepartments()
        }

    suspend fun createUser(
        request: CreateUserRequest
    ): ClearanceResult<MessageResponse> =
        safeCall("createUser") {
            apiService.createUser(request)
        }

    suspend fun updateUser(
        id: Int,
        request: UpdateUserRequest
    ): ClearanceResult<MessageResponse> =
        safeCall("updateUser") {
            apiService.updateUser(id, request)
        }

    suspend fun deleteUser(
        id: Int
    ): ClearanceResult<MessageResponse> =
        safeCall("deleteUser") {
            apiService.deleteUser(id)
        }

    suspend fun activateUser(
        userId: Int
    ): ClearanceResult<MessageResponse> =
        safeCall("activateUser") {
            apiService.activateUser(userId)
        }

    suspend fun deactivateUser(
        id: Int
    ): ClearanceResult<MessageResponse> =
        safeCall("deactivateUser") {
            apiService.deactivateUser(id)
        }

    suspend fun finalApprove(
        clearanceId: Int
    ): ClearanceResult<MessageResponse> =
        safeCall("finalApprove") {
            apiService.finalApproveClearance(clearanceId)
        }

    suspend fun getClearanceReport(): ClearanceResult<List<DepartmentReportDto>> =
        safeCall("getClearanceReport") {
            apiService.getClearanceReport()
        }

    /**
     * Centralized API error handling.
     */
    private suspend fun <T> safeCall(
        label: String,
        call: suspend () -> Response<T>
    ): ClearanceResult<T> {

        Log.d(TAG, "$label: starting request")

        return try {

            val response = call()

            Log.d(
                TAG,
                "$label: HTTP ${response.code()}, " +
                        "isSuccessful=${response.isSuccessful}"
            )

            if (response.isSuccessful) {

                val body = response.body()

                if (body != null) {

                    Log.d(
                        TAG,
                        "$label: success, body present"
                    )

                    ClearanceResult.Success(body)

                } else {

                    Log.e(
                        TAG,
                        "$label: success but body is null"
                    )

                    ClearanceResult.Failure(
                        "Server returned an empty response."
                    )
                }

            } else {

                val errorBodyString =
                    response.errorBody()?.string()

                Log.e(
                    TAG,
                    "$label: HTTP ${response.code()} " +
                            "error body: $errorBodyString"
                )

                val backendMessage =
                    extractMessage(errorBodyString)

                ClearanceResult.Failure(
                    backendMessage
                        ?: "Request failed (HTTP ${response.code()})."
                )
            }

        } catch (e: IOException) {

            Log.e(
                TAG,
                "$label: IOException - ${e.message}",
                e
            )

            ClearanceResult.Failure(
                "Could not reach the server. Check that the backend is " +
                        "running and that your phone is on the same Wi-Fi network."
            )

        } catch (e: Exception) {

            Log.e(
                TAG,
                "$label: Exception - " +
                        "${e::class.simpleName}: ${e.message}",
                e
            )

            ClearanceResult.Failure(
                "Something went wrong. Please try again."
            )
        }
    }

    private fun extractMessage(
        errorBody: String?
    ): String? {

        if (errorBody.isNullOrBlank()) {
            return null
        }

        return try {

            val json =
                JsonParser.parseString(errorBody)
                    .asJsonObject

            if (json.has("message")) {
                json.get("message").asString
            } else {
                null
            }

        } catch (e: Exception) {
            null
        }
    }
}