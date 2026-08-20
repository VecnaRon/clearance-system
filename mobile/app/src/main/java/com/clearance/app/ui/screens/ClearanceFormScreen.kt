package com.clearance.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clearance.app.viewmodel.ClearanceFormViewModel
import kotlinx.coroutines.delay

private val REASON_OPTIONS = listOf(
    "kcse_completion" to "KCSE Completion",
    "transfer" to "Transfer",
    "withdrawal" to "Withdrawal",
    "other" to "Other"
)

/**
 * Reproduces client/src/pages/student/ClearanceForm.js.
 */
@Composable
fun ClearanceFormScreen(
    viewModel: ClearanceFormViewModel = viewModel(),
    onNavigateToStatus: () -> Unit
) {
    val uiState = viewModel.uiState

    // Matches ClearanceForm.js's setTimeout(() => navigate("/student/status"), 2000)
    LaunchedEffect(uiState.submittedClearanceId) {
        if (uiState.submittedClearanceId != null) {
            delay(2000)
            onNavigateToStatus()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                uiState.isLoadingInitial -> LoadingContent()
                uiState.loadError != null -> ErrorContent(uiState.loadError)
                uiState.hasActiveClearance -> ActiveClearanceContent(onViewStatus = onNavigateToStatus)
                uiState.submittedClearanceId != null -> SubmittedContent(uiState.submittedClearanceId)
                else -> FormContent(viewModel)
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Text("Loading...", modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/** Matches ClearanceForm.js's "You Already Have an Active Clearance" card. */
@Composable
private fun ActiveClearanceContent(onViewStatus: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "\uD83D\uDCCB", fontSize = androidx.compose.ui.unit.TextUnit.Unspecified)
            Text(
                text = "You Already Have an Active Clearance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = "You have already submitted a clearance request. " +
                        "Track your progress on the status page.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
            Button(onClick = onViewStatus) {
                Text("View Clearance Status \u2192")
            }
        }
    }
}

/** Matches ClearanceForm.js's success message ("Clearance started. Ref: #X"). */
@Composable
private fun SubmittedContent(clearanceId: Int?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Clearance started. Ref: #$clearanceId",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormContent(viewModel: ClearanceFormViewModel) {
    val uiState = viewModel.uiState
    var dropdownExpanded by remember { mutableStateOf(false) }
    val selectedLabel = REASON_OPTIONS.first { it.first == uiState.reason }.second

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Student Clearance Request Form",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Name: ${uiState.fullName}   |   Admission: ${uiState.admissionNumber}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Text(text = "Reason for Clearance", style = MaterialTheme.typography.labelLarge)
        ExposedDropdownMenuBox(
            expanded = dropdownExpanded,
            onExpandedChange = { dropdownExpanded = it },
            modifier = Modifier.padding(top = 4.dp)
        ) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }
            ) {
                REASON_OPTIONS.forEach { (value, label) ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            viewModel.onReasonChange(value)
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        if (uiState.reason == "other") {
            Text(
                text = "Other (specify)",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 16.dp)
            )
            OutlinedTextField(
                value = uiState.reasonOther,
                onValueChange = viewModel::onReasonOtherChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        if (uiState.submitError != null) {
            Text(
                text = uiState.submitError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Button(
            onClick = viewModel::submit,
            enabled = !uiState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
                Text("Submitting...")
            } else {
                Text("Submit")
            }
        }
    }
}