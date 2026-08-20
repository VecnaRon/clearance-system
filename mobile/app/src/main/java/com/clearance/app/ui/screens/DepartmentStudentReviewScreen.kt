package com.clearance.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clearance.app.data.api.dto.DepartmentStepDetailDto
import com.clearance.app.data.api.dto.DepartmentStepRecordDto
import com.clearance.app.ui.theme.ClearanceDanger
import com.clearance.app.ui.theme.ClearanceInputBackground
import com.clearance.app.ui.theme.ClearanceInputLabel
import com.clearance.app.ui.theme.ClearancePrimary
import com.clearance.app.ui.theme.ClearanceScreenBackground
import com.clearance.app.ui.theme.ClearanceSnackbarBackground
import com.clearance.app.ui.theme.ClearanceSnackbarText
import com.clearance.app.ui.theme.ClearanceSuccess
import com.clearance.app.ui.theme.ClearanceSurface
import com.clearance.app.ui.theme.ClearanceTextMuted
import com.clearance.app.ui.theme.ClearanceTextPrimary
import com.clearance.app.ui.theme.ClearanceTextSecondary
import com.clearance.app.ui.theme.ClearanceWarning
import com.clearance.app.viewmodel.DepartmentStudentReviewViewModel

/**
 * Reproduces client/src/pages/department/DeptStudentReview.js.
 *
 * The three real action buttons (Mark as Cleared / Reject / Save as
 * Pending) are reproduced as-is — the web app's status <select> is
 * dead code (see contract note above) and is deliberately NOT ported
 * as a functional control.
 */
@Composable
fun DepartmentStudentReviewScreen(
    stepId: Int,
    viewModel: DepartmentStudentReviewViewModel = viewModel(),
    onDecisionComplete: () -> Unit
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(stepId) {
        viewModel.load(stepId)
    }

    LaunchedEffect(uiState.decisionSuccessMessage) {
        val message = uiState.decisionSuccessMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeSuccess()
            onDecisionComplete()
        }
    }

    Scaffold(
        containerColor = ClearanceScreenBackground,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = ClearanceSnackbarBackground,
                    contentColor = ClearanceSnackbarText
                )
            }
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(color = ClearancePrimary)
                    Text(
                        text = "Loading student details...",
                        color = ClearanceTextMuted,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
            uiState.loadError != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = uiState.loadError, color = ClearanceDanger, fontSize = 14.sp)
                }
            }
            uiState.detail != null -> {
                ReviewContent(
                    detail = uiState.detail,
                    remarks = uiState.remarks,
                    isSubmitting = uiState.isSubmitting,
                    submitError = uiState.submitError,
                    onRemarksChange = viewModel::onRemarksChange,
                    onSubmit = { status -> viewModel.submitDecision(stepId, status) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ReviewContent(
    detail: DepartmentStepDetailDto,
    remarks: String,
    isSubmitting: Boolean,
    submitError: String?,
    onRemarksChange: (String) -> Unit,
    onSubmit: (String) -> Unit,
    modifier: Modifier
) {
    val hasUnresolved = detail.records.any { it.status == "unresolved" }

    Column(modifier = modifier) {
        StudentHeader(detail)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ClearanceSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Review Clearance", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ClearanceTextPrimary)
                    StatusPill(status = detail.status)
                }

                Text(
                    text = "Remarks & Comments",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ClearanceTextMuted,
                    modifier = Modifier.padding(top = 20.dp, bottom = 6.dp)
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = onRemarksChange,
                    placeholder = { Text("Add any comments or reasons for your decision...") },
                    enabled = !isSubmitting,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ClearanceTextPrimary,
                        unfocusedTextColor = ClearanceTextPrimary,
                        focusedBorderColor = ClearancePrimary,
                        unfocusedBorderColor = ClearanceTextMuted,
                        focusedContainerColor = ClearanceInputBackground,
                        unfocusedContainerColor = ClearanceInputBackground,
                        cursorColor = ClearancePrimary
                    ),
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )

                if (submitError != null) {
                    Text(
                        text = submitError,
                        color = ClearanceDanger,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        RecordsSection(records = detail.records)
        Spacer(modifier = Modifier.height(20.dp))
        DecisionButtons(
            isSubmitting = isSubmitting,
            hasUnresolved = hasUnresolved,
            onSubmit = onSubmit
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StudentHeader(detail: DepartmentStepDetailDto) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ClearanceTextPrimary)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = detail.full_name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            text = detail.admission_number,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        Text(
            text = detail.department_name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.15f))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun StatusPill(status: String) {
    val color = when (status) {
        "approved" -> ClearanceSuccess
        "rejected" -> ClearanceDanger
        else -> ClearanceWarning
    }
    Text(
        text = status.uppercase(),
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun RecordsSection(records: List<DepartmentStepRecordDto>) {
    if (records.isEmpty()) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ClearanceSuccess.copy(alpha = 0.1f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "\u2705 No outstanding records for this student",
                color = ClearanceSuccess,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val totalOutstanding = records
        .filter { it.status == "unresolved" && it.amount != null }
        .sumOf { it.amount ?: 0.0 }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ClearanceWarning.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "\u26A0\uFE0F Outstanding Department Records (${records.size})",
                color = ClearanceTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            records.forEach { record ->
                RecordRow(record)
            }
            Text(
                text = "Total Outstanding: KES ${totalOutstanding}",
                color = ClearanceTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun RecordRow(record: DepartmentStepRecordDto) {
    val resolved = record.status == "resolved"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ClearanceSurface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = record.description, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = ClearanceTextPrimary)
            Text(
                text = record.status.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (resolved) ClearanceSuccess else ClearanceDanger
            )
        }
        Text(
            text = if (record.amount != null) "KES ${record.amount}" else "\u2014",
            fontSize = 12.sp,
            color = ClearanceTextSecondary
        )
        Text(
            text = "Logged by ${record.loggedBy} \u2022 ${formatDateOnly(record.createdAt)}",
            fontSize = 11.sp,
            color = ClearanceTextMuted
        )
    }
}

@Composable
private fun DecisionButtons(
    isSubmitting: Boolean,
    hasUnresolved: Boolean,
    onSubmit: (String) -> Unit
) {
    Column {
        Button(
            onClick = { onSubmit("cleared") },
            enabled = !isSubmitting && !hasUnresolved,
            colors = ButtonDefaults.buttonColors(containerColor = ClearanceSuccess, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.width(16.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Text("\u2713 Mark as Cleared")
            }
        }
        if (hasUnresolved) {
            Text(
                text = "Cannot clear — this student has unresolved records in this department.",
                color = ClearanceDanger,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Button(
            onClick = { onSubmit("rejected") },
            enabled = !isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = ClearanceDanger, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Text("\u2717 Reject Clearance")
        }

        TextButton(
            onClick = { onSubmit("pending") },
            enabled = !isSubmitting,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("\uD83D\uDCBE Save as Pending", color = ClearanceTextSecondary, fontWeight = FontWeight.SemiBold)
        }
    }
}

/**
 * Same substring-based date formatting used in
 * StudentClearanceStatusScreen.kt — ISO-8601 string, first 10 chars.
 */
private fun formatDateOnly(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "\u2014"
    return if (isoDate.length >= 10) isoDate.substring(0, 10) else isoDate
}