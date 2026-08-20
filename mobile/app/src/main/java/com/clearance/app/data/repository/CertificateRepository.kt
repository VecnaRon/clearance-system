package com.clearance.app.data.repository

import com.clearance.app.data.api.ApiService
import com.clearance.app.data.api.RetrofitClient
import com.google.gson.JsonParser
import okhttp3.ResponseBody
import java.io.IOException

sealed class CertificateResult {
    data class Success(val body: ResponseBody) : CertificateResult()
    data class Failure(val message: String) : CertificateResult()
}

/**
 * Handles GET /api/clearance/{id}/certificate only. Deliberately has
 * no Android Context dependency — file saving is the ViewModel's job
 * (via AndroidViewModel), keeping this class a pure network layer,
 * consistent with AuthRepository and ClearanceRepository.
 */
class CertificateRepository(
    private val apiService: ApiService = RetrofitClient.apiService
) {

    suspend fun downloadCertificate(clearanceId: Int): CertificateResult {
        return try {
            val response = apiService.downloadCertificate(clearanceId)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    CertificateResult.Success(body)
                } else {
                    CertificateResult.Failure("Server returned an empty certificate response.")
                }
            } else {
                val backendMessage = extractMessage(response.errorBody()?.string())
                val fallback = when (response.code()) {
                    403 -> "Certificate not available. Clearance is not fully approved."
                    404 -> "Clearance not found."
                    else -> "Failed to download certificate (HTTP ${response.code()})."
                }
                CertificateResult.Failure(backendMessage ?: fallback)
            }
        } catch (e: IOException) {
            CertificateResult.Failure(
                "Could not reach the server. Check that the backend is " +
                        "running and that your phone is on the same Wi-Fi network."
            )
        } catch (e: Exception) {
            CertificateResult.Failure("Something went wrong while downloading the certificate.")
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