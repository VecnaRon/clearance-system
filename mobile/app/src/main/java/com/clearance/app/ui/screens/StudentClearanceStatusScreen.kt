package com.clearance.app.ui.screens

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clearance.app.ai.AIAdvisorEngine
import com.clearance.app.ai.AIAdvisorResult
import com.clearance.app.ai.RiskLevel
import com.clearance.app.data.api.dto.ClearanceDepartmentDto
import com.clearance.app.data.api.dto.ClearanceDto
import com.clearance.app.data.api.dto.RecordDto
import com.clearance.app.ui.theme.ClearanceAccent
import com.clearance.app.ui.theme.ClearanceDanger
import com.clearance.app.ui.theme.ClearanceGray100
import com.clearance.app.ui.theme.ClearanceInfo
import com.clearance.app.ui.theme.ClearancePrimary
import com.clearance.app.ui.theme.ClearanceSuccess
import com.clearance.app.ui.theme.ClearanceSurface
import com.clearance.app.ui.theme.ClearanceTextMuted
import com.clearance.app.ui.theme.ClearanceTextPrimary
import com.clearance.app.ui.theme.ClearanceTextSecondary
import com.clearance.app.ui.theme.ClearanceWarning
import com.clearance.app.viewmodel.ClearanceStatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentClearanceStatusScreen(
    viewModel: ClearanceStatusViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current

    // PROXIMITY-BASED PRIVACY MODE (Phase 2).
    // Registered only while this screen is composed; unregistered on
    // dispose. State only flips on an actual near/far transition
    // (guarded inside the listener) to avoid flicker from repeated
    // identical sensor events. Purely local UI state — no data is
    // cleared, no API call is made, no backend/database is touched.
    var isPrivacyModeActive by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        var lastIsNear: Boolean? = null

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val maxRange = proximitySensor?.maximumRange ?: event.values[0]
                val currentlyNear = event.values[0] < maxRange
                if (currentlyNear != lastIsNear) {
                    lastIsNear = currentlyNear
                    isPrivacyModeActive = currentlyNear
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* not needed */ }
        }

        if (proximitySensor != null) {
            sensorManager.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Status", color = ClearanceTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("\u2190", color = ClearanceTextPrimary, fontSize = 20.sp)
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = !uiState.isRefreshing) {
                        Text(
                            text = if (uiState.isRefreshing) "\u23F3" else "\u27F3",
                            color = ClearancePrimary,
                            fontSize = 18.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClearanceSurface,
                    titleContentColor = ClearanceTextPrimary
                )
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.clearance == null || uiState.clearance.id == null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No Clearance Request Found",
                        style = MaterialTheme.typography.titleLarge,
                        color = ClearanceTextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "You haven't submitted a clearance request yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClearanceTextMuted,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )
                    TextButton(onClick = viewModel::refresh, enabled = !uiState.isRefreshing) {
                        Text(if (uiState.isRefreshing) "Refreshing..." else "\u27F3 Refresh")
                    }
                }
            }
            else -> {
                StatusContent(
                    clearance = uiState.clearance,
                    records = uiState.records,
                    isDownloadingCertificate = uiState.isDownloadingCertificate,
                    downloadError = uiState.downloadError,
                    downloadedFileUri = uiState.downloadedFileUri,
                    onDownloadCertificate = viewModel::downloadCertificate,
                    isPrivacyModeActive = isPrivacyModeActive,
                    modifier = Modifier.fillMaxSize().padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun StatusContent(
    clearance: ClearanceDto,
    records: List<RecordDto>,
    isDownloadingCertificate: Boolean,
    downloadError: String?,
    downloadedFileUri: Uri?,
    onDownloadCertificate: () -> Unit,
    isPrivacyModeActive: Boolean,
    modifier: Modifier
) {
    val departments = clearance.departments ?: emptyList()
    val clearedCount = departments.count { it.status == "approved" }
    val totalCount = departments.size
    val aiResult = AIAdvisorEngine.analyze(clearance, records)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        HeaderCard()
        Spacer(modifier = Modifier.height(16.dp))

        if (isPrivacyModeActive) {
            PrivacyModeCard()
        } else {
            InfoAndAlertsCard(
                clearance = clearance,
                clearedCount = clearedCount,
                totalCount = totalCount,
                isDownloadingCertificate = isDownloadingCertificate,
                downloadError = downloadError,
                downloadedFileUri = downloadedFileUri,
                onDownloadCertificate = onDownloadCertificate
            )
            if (aiResult != null) {
                Spacer(modifier = Modifier.height(16.dp))
                AIAdvisorCard(aiResult)
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutstandingItemsCard(records)
            if (departments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                DepartmentDetailsCard(departments)
            }
        }
    }
}

/** Shown instead of all sensitive clearance content while proximity reports NEAR. */
@Composable
private fun PrivacyModeCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "\uD83D\uDD12", fontSize = 40.sp)
            Text(
                text = "Privacy Mode",
                color = ClearanceTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
            Text(
                text = "Clearance information is temporarily hidden because the proximity sensor detected an object near the device.",
                color = ClearanceTextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = "Move your hand away to continue.",
                color = ClearanceTextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun AIAdvisorCard(result: AIAdvisorResult) {
    val riskColor = when (result.riskLevel) {
        RiskLevel.LOW -> ClearanceSuccess
        RiskLevel.MEDIUM -> ClearanceWarning
        RiskLevel.HIGH -> ClearanceDanger
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "\uD83E\uDD16 AI Clearance Advisor",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = ClearanceTextPrimary
            )
            Text(
                text = "Your clearance is ${result.progressPercent}% complete (${result.clearedCount}/${result.totalCount} departments)",
                fontSize = 13.sp,
                color = ClearanceTextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                AdvisorStat(label = "Risk", value = result.riskLevel.name, color = riskColor, modifier = Modifier.weight(1f))
                AdvisorStat(label = "Readiness", value = "${result.readinessScore}%", color = ClearancePrimary, modifier = Modifier.weight(1f))
            }
            Text(
                text = result.riskExplanation,
                fontSize = 12.sp,
                color = ClearanceTextMuted,
                modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)
            )

            if (result.priority != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ClearanceInfo.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(text = "Priority: ${result.priority.label}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ClearanceTextPrimary)
                    Text(text = result.priority.reason, fontSize = 12.sp, color = ClearanceTextSecondary, modifier = Modifier.padding(top = 2.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(text = "Recommendations", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ClearanceTextMuted, modifier = Modifier.padding(bottom = 6.dp))
            result.recommendations.forEach { rec ->
                Text(text = "\u2022 $rec", fontSize = 13.sp, color = ClearanceTextSecondary, modifier = Modifier.padding(bottom = 4.dp))
            }

            Text(
                text = "Estimated completion: ${result.estimatedCompletionText}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClearanceTextMuted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun AdvisorStat(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(10.dp)
    ) {
        Text(text = label.uppercase(), fontSize = 10.sp, color = ClearanceTextMuted, fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
private fun HeaderCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(ClearancePrimary, ClearanceAccent),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Clearance Status",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Track your clearance progress in real-time",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun InfoAndAlertsCard(
    clearance: ClearanceDto,
    clearedCount: Int,
    totalCount: Int,
    isDownloadingCertificate: Boolean,
    downloadError: String?,
    downloadedFileUri: Uri?,
    onDownloadCertificate: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            InfoRow("Admission Number", clearance.admission_number ?: "\u2014")
            InfoRow("Current Status", (clearance.status ?: "\u2014").replace("_", " "))
            InfoRow("Submitted Date", formatDateOnly(clearance.submitted_at))
            InfoRow("Progress", "$clearedCount/$totalCount Departments")

            when (clearance.status) {
                "awaiting_final" -> AlertBox(
                    color = ClearanceInfo,
                    title = "\uD83C\uDF93 Awaiting Final Approval",
                    body = "Congratulations! All departments have cleared you. Your " +
                            "clearance is now awaiting final approval from the Principal."
                )
                "approved" -> AlertBox(
                    color = ClearanceSuccess,
                    title = "\u2705 Clearance Completed!",
                    body = "Your clearance has been fully approved. You can now " +
                            "download your clearance certificate."
                ) {
                    DownloadCertificateSection(
                        isDownloading = isDownloadingCertificate,
                        downloadError = downloadError,
                        downloadedFileUri = downloadedFileUri,
                        onDownloadCertificate = onDownloadCertificate
                    )
                }
                else -> { /* pending: no alert box, matches web app */ }
            }
        }
    }
}

@Composable
private fun DownloadCertificateSection(
    isDownloading: Boolean,
    downloadError: String?,
    downloadedFileUri: Uri?,
    onDownloadCertificate: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(top = 12.dp)) {
        Button(
            onClick = onDownloadCertificate,
            enabled = !isDownloading,
            colors = ButtonDefaults.buttonColors(containerColor = ClearanceSuccess)
        ) {
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
                Text("Downloading...")
            } else {
                Text("\uD83D\uDCC4 Download Certificate")
            }
        }

        if (downloadError != null) {
            Text(
                text = downloadError,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (downloadedFileUri != null) {
            Text(
                text = "Certificate saved. You can find it in your Downloads.",
                color = ClearanceSuccess,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
            if (downloadedFileUri.scheme == "content") {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(downloadedFileUri, "application/pdf")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text("Open PDF")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = label.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ClearanceTextMuted
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ClearanceTextPrimary
        )
    }
}

@Composable
private fun AlertBox(
    color: Color,
    title: String,
    body: String,
    extra: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(16.dp)
    ) {
        Text(text = title, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(
            text = body,
            color = ClearanceTextSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
        extra?.invoke()
    }
}

@Composable
private fun OutstandingItemsCard(records: List<RecordDto>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "\u26A0\uFE0F My Outstanding Items",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (records.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ClearanceSuccess.copy(alpha = 0.1f))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "\u2705", fontSize = 28.sp)
                    Text(
                        text = "No Outstanding Items",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Text(
                        text = "No departments have flagged any items against your account.",
                        fontSize = 13.sp,
                        color = ClearanceTextMuted
                    )
                }
            } else {
                val unresolved = records.count { it.status == "unresolved" }
                val resolved = records.count { it.status == "resolved" }
                val allClear = unresolved == 0

                Text(
                    text = if (allClear) {
                        "\u2705 All ${records.size} item(s) have been resolved."
                    } else {
                        "\u26A0\uFE0F You have $unresolved unresolved item(s) and $resolved resolved item(s)."
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (allClear) ClearanceSuccess.copy(alpha = 0.1f) else ClearanceWarning.copy(alpha = 0.15f)
                        )
                        .padding(12.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                records.forEach { item ->
                    OutstandingItemRow(item)
                }
            }
        }
    }
}

@Composable
private fun OutstandingItemRow(item: RecordDto) {
    val resolved = item.status == "resolved"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (resolved) ClearanceSuccess.copy(alpha = 0.08f) else ClearanceDanger.copy(alpha = 0.06f))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.description ?: "",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
            Text(
                text = "Dept: Department #${item.departmentId ?: "?"}",
                fontSize = 12.sp,
                color = ClearanceTextMuted
            )
            if (item.amount != null) {
                Text(
                    text = "Amount: KES ${item.amount}",
                    fontSize = 12.sp,
                    color = ClearanceTextMuted
                )
            }
            Text(
                text = "Logged: ${formatDateOnly(item.createdAt)}",
                fontSize = 12.sp,
                color = ClearanceTextMuted
            )
            if (item.resolvedAt != null) {
                Text(
                    text = "Resolved: ${formatDateOnly(item.resolvedAt)}",
                    fontSize = 12.sp,
                    color = ClearanceTextMuted
                )
            }
        }
        Text(
            text = item.status.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (resolved) ClearanceSuccess else ClearanceDanger
        )
    }
}

@Composable
private fun DepartmentDetailsCard(departments: List<ClearanceDepartmentDto>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Department Status Details",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            departments.forEach { dept ->
                DepartmentRow(dept)
            }
        }
    }
}

@Composable
private fun DepartmentRow(dept: ClearanceDepartmentDto) {
    val borderColor = when (dept.status) {
        "approved" -> ClearanceSuccess
        "rejected" -> ClearanceDanger
        else -> ClearanceGray100
    }
    val badgeColor = when (dept.status) {
        "approved" -> ClearanceSuccess
        "rejected" -> ClearanceDanger
        "awaiting_final" -> ClearanceInfo
        else -> ClearanceWarning
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(8.dp))
            .background(ClearanceGray100)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = dept.department_name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = dept.status.replace("_", " ").uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
        if (dept.remarks.isNotBlank()) {
            Text(
                text = "Remarks: ${dept.remarks}",
                fontSize = 13.sp,
                color = ClearanceTextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        if (dept.has_dues) {
            Text(
                text = "\uD83D\uDCB0 Outstanding Dues: KSh ${dept.dues_amount}",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClearanceDanger,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

private fun formatDateOnly(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "\u2014"
    return if (isoDate.length >= 10) isoDate.substring(0, 10) else isoDate
}