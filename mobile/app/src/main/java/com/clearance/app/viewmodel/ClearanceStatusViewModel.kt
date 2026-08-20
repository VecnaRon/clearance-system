package com.clearance.app.viewmodel

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.clearance.app.data.api.dto.ClearanceDto
import com.clearance.app.data.api.dto.RecordDto
import com.clearance.app.data.repository.CertificateRepository
import com.clearance.app.data.repository.CertificateResult
import com.clearance.app.data.repository.ClearanceRepository
import com.clearance.app.data.repository.ClearanceResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

data class ClearanceStatusUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val clearance: ClearanceDto? = null,
    val records: List<RecordDto> = emptyList(),
    val isDownloadingCertificate: Boolean = false,
    val downloadError: String? = null,
    val downloadedFileUri: Uri? = null
)

private sealed class SaveResult {
    data class Success(val uri: Uri) : SaveResult()
    data class Failure(val message: String) : SaveResult()
}

/**
 * IMPORTANT: this class must keep a constructor with ONLY an
 * Application parameter. AndroidViewModel's default factory
 * instantiates subclasses reflectively by looking for exactly that
 * single-parameter constructor — adding repository parameters here
 * (even with Kotlin default values) would break that lookup at
 * runtime. The repositories are therefore plain body properties.
 *
 * Mirrors client/src/pages/student/ClearanceStatus.js: on load,
 * fetches /clearance/my-latest and /records/mine. MILESTONE 4.5 adds
 * refresh() (re-calls the same two endpoints — the web app has no
 * refresh mechanism at all, but mobile has no page-reload equivalent,
 * so this reuses only already-verified calls) and
 * downloadCertificate() (calls /clearance/{id}/certificate and saves
 * the PDF, matching ClearanceStatus.js's own inline downloadCertificate()).
 */
class ClearanceStatusViewModel(application: Application) : AndroidViewModel(application) {

    private val clearanceRepository = ClearanceRepository()
    private val certificateRepository = CertificateRepository()

    var uiState by mutableStateOf(ClearanceStatusUiState())
        private set

    init {
        loadAll(initial = true)
    }

    fun refresh() {
        if (uiState.isRefreshing) return
        loadAll(initial = false)
    }

    private fun loadAll(initial: Boolean) {
        viewModelScope.launch {
            uiState = if (initial) {
                uiState.copy(isLoading = true)
            } else {
                uiState.copy(isRefreshing = true)
            }

            val clearanceResult = clearanceRepository.getMyLatestClearance()
            val recordsResult = clearanceRepository.getMyRecords()

            val newClearance = when (clearanceResult) {
                is ClearanceResult.Success -> clearanceResult.data
                is ClearanceResult.Failure -> null
            }
            val newRecords = when (recordsResult) {
                is ClearanceResult.Success -> recordsResult.data
                is ClearanceResult.Failure -> emptyList()
            }

            uiState = uiState.copy(
                isLoading = false,
                isRefreshing = false,
                clearance = newClearance,
                records = newRecords
            )
        }
    }

    fun downloadCertificate() {
        val clearanceId = uiState.clearance?.id ?: return
        if (uiState.isDownloadingCertificate) return

        viewModelScope.launch {
            uiState = uiState.copy(
                isDownloadingCertificate = true,
                downloadError = null,
                downloadedFileUri = null
            )

            when (val result = certificateRepository.downloadCertificate(clearanceId)) {
                is CertificateResult.Success -> {
                    val bytes = withContext(Dispatchers.IO) { result.body.bytes() }
                    val admission = uiState.clearance?.admission_number ?: clearanceId.toString()
                    val fileName = "clearance_certificate_$admission.pdf"

                    when (val saveResult = withContext(Dispatchers.IO) { saveCertificate(bytes, fileName) }) {
                        is SaveResult.Success -> {
                            uiState = uiState.copy(
                                isDownloadingCertificate = false,
                                downloadedFileUri = saveResult.uri
                            )
                        }
                        is SaveResult.Failure -> {
                            uiState = uiState.copy(
                                isDownloadingCertificate = false,
                                downloadError = saveResult.message
                            )
                        }
                    }
                }
                is CertificateResult.Failure -> {
                    uiState = uiState.copy(isDownloadingCertificate = false, downloadError = result.message)
                }
            }
        }
    }

    /**
     * API 29+: saves into the public Downloads collection via
     * MediaStore — no permission required, immediately visible in any
     * Files/Downloads app, returns a content:// Uri that can be opened
     * directly.
     *
     * Below API 29: saves into the app's own external files directory
     * (also no permission required) — not visible outside the app, so
     * the caller does not attempt to open it, only reports where it
     * went, avoiding a FileUriExposedException risk from a raw
     * file:// Uri Intent on this project's targetSdk.
     */
    private fun saveCertificate(bytes: ByteArray, fileName: String): SaveResult {
        val context = getApplication<Application>()
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val itemUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return SaveResult.Failure("Could not create the file in Downloads.")

                val outputStream = resolver.openOutputStream(itemUri)
                    ?: return SaveResult.Failure("Could not open the file for writing.")
                outputStream.use { it.write(bytes) }

                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(itemUri, values, null, null)

                SaveResult.Success(itemUri)
            } else {
                val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: return SaveResult.Failure("Could not access app storage.")
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { it.write(bytes) }
                SaveResult.Success(Uri.fromFile(file))
            }
        } catch (e: IOException) {
            SaveResult.Failure("Failed to save the certificate file.")
        } catch (e: Exception) {
            SaveResult.Failure("Failed to save the certificate file.")
        }
    }
}