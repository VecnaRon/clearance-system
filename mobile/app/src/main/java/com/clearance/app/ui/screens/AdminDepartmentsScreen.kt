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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
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
import com.clearance.app.data.api.dto.DepartmentDto
import com.clearance.app.ui.navigation.AdminNavActions
import com.clearance.app.ui.navigation.AppDrawerScaffold
import com.clearance.app.ui.theme.ClearanceDanger
import com.clearance.app.ui.theme.ClearanceDialogBackground
import com.clearance.app.ui.theme.ClearanceInputBackground
import com.clearance.app.ui.theme.ClearanceInputLabel
import com.clearance.app.ui.theme.ClearancePrimary
import com.clearance.app.ui.theme.ClearanceSnackbarBackground
import com.clearance.app.ui.theme.ClearanceSnackbarText
import com.clearance.app.ui.theme.ClearanceSurface
import com.clearance.app.ui.theme.ClearanceTextMuted
import com.clearance.app.ui.theme.ClearanceTextPrimary
import com.clearance.app.ui.theme.ClearanceTextSecondary
import com.clearance.app.viewmodel.DepartmentViewModel

@Composable
fun AdminDepartmentsScreen(
    viewModel: DepartmentViewModel = viewModel(),
    adminNav: AdminNavActions
) {
    val uiState = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        val message = uiState.successMessage ?: uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
    }

    AppDrawerScaffold(
        title = "Manage Departments",
        drawerItems = adminDrawerItems(adminNav, selected = "Departments"),
        onLogout = adminNav.onLogout,
        topBarActions = {
            IconButton(onClick = viewModel::openCreateDialog) {
                Text("+", color = ClearancePrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(color = ClearancePrimary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Manage the departments used in the school clearance workflow.",
                    fontSize = 14.sp,
                    color = ClearanceTextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                TextButton(onClick = viewModel::refresh, enabled = !uiState.isRefreshing) {
                    Text(if (uiState.isRefreshing) "Refreshing..." else "\u27F3 Refresh", color = ClearancePrimary)
                }

                if (uiState.departments.isEmpty()) {
                    Text(
                        text = "No departments found. Tap + to add one.",
                        fontSize = 14.sp,
                        color = ClearanceTextMuted,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    uiState.departments.forEach { dept ->
                        DepartmentCard(
                            department = dept,
                            onEdit = { viewModel.openEditDialog(dept) },
                            onDelete = { viewModel.requestDelete(dept) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        SnackbarHost(hostState = snackbarHostState) { data ->
            Snackbar(snackbarData = data, containerColor = ClearanceSnackbarBackground, contentColor = ClearanceSnackbarText)
        }
    }

    if (uiState.showFormDialog) {
        DepartmentFormDialog(viewModel = viewModel)
    }
    if (uiState.deletingDepartment != null) {
        DeleteConfirmDialog(viewModel = viewModel)
    }
}

@Composable
private fun DepartmentCard(department: DepartmentDto, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ClearanceSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = department.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ClearanceTextPrimary)
                    if (!department.code.isNullOrBlank()) {
                        Text(
                            text = department.code,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ClearancePrimary,
                            modifier = Modifier.clip(RoundedCornerShape(50)).background(ClearancePrimary.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            if (!department.description.isNullOrBlank()) {
                Text(text = department.description, fontSize = 13.sp, color = ClearanceTextSecondary, modifier = Modifier.padding(top = 8.dp))
            }
            Row(modifier = Modifier.padding(top = 12.dp)) {
                TextButton(onClick = onEdit) { Text("Edit", color = ClearancePrimary, fontWeight = FontWeight.SemiBold) }
                TextButton(onClick = onDelete) { Text("Delete", color = ClearanceDanger, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun clearanceTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = ClearanceTextPrimary,
    unfocusedTextColor = ClearanceTextPrimary,
    disabledTextColor = ClearanceTextMuted,
    focusedLabelColor = ClearancePrimary,
    unfocusedLabelColor = ClearanceInputLabel,
    disabledLabelColor = ClearanceTextMuted,
    focusedBorderColor = ClearancePrimary,
    unfocusedBorderColor = ClearanceTextMuted,
    disabledBorderColor = ClearanceTextMuted,
    focusedContainerColor = ClearanceInputBackground,
    unfocusedContainerColor = ClearanceInputBackground,
    disabledContainerColor = ClearanceInputBackground,
    cursorColor = ClearancePrimary
)

@Composable
private fun DepartmentFormDialog(viewModel: DepartmentViewModel) {
    val uiState = viewModel.uiState
    val form = uiState.formState
    val isEditing = form.editingId != null
    val fieldColors = clearanceTextFieldColors()

    AlertDialog(
        onDismissRequest = viewModel::dismissFormDialog,
        containerColor = ClearanceDialogBackground,
        titleContentColor = ClearanceTextPrimary,
        textContentColor = ClearanceTextSecondary,
        title = { Text(if (isEditing) "Edit Department" else "Add Department", fontWeight = FontWeight.Bold, color = ClearanceTextPrimary) },
        text = {
            Column {
                OutlinedTextField(
                    value = form.name, onValueChange = viewModel::onFormNameChange, label = { Text("Name *") },
                    singleLine = true, enabled = !uiState.isSavingForm, colors = fieldColors, modifier = Modifier.fillMaxWidth()
                )
                if (!isEditing) {
                    OutlinedTextField(
                        value = form.code, onValueChange = viewModel::onFormCodeChange, label = { Text("Code *") },
                        singleLine = true, enabled = !uiState.isSavingForm, colors = fieldColors,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                }
                OutlinedTextField(
                    value = form.description, onValueChange = viewModel::onFormDescriptionChange, label = { Text("Description") },
                    enabled = !uiState.isSavingForm, colors = fieldColors, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                if (uiState.formError != null) {
                    Text(text = uiState.formError, color = ClearanceDanger, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = viewModel::submitForm, enabled = !uiState.isSavingForm,
                colors = ButtonDefaults.buttonColors(containerColor = ClearancePrimary, contentColor = Color.White)
            ) {
                if (uiState.isSavingForm) {
                    CircularProgressIndicator(modifier = Modifier.width(16.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text(if (isEditing) "Save" else "Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::dismissFormDialog, enabled = !uiState.isSavingForm) {
                Text("Cancel", color = ClearanceTextSecondary)
            }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(viewModel: DepartmentViewModel) {
    val uiState = viewModel.uiState
    val target = uiState.deletingDepartment ?: return

    AlertDialog(
        onDismissRequest = viewModel::dismissDeleteDialog,
        containerColor = ClearanceDialogBackground,
        titleContentColor = ClearanceTextPrimary,
        textContentColor = ClearanceTextSecondary,
        title = { Text("Delete Department?", fontWeight = FontWeight.Bold, color = ClearanceTextPrimary) },
        text = { Text("This action will permanently remove \"${target.name}\". Continue?", color = ClearanceTextSecondary) },
        confirmButton = {
            Button(
                onClick = viewModel::confirmDelete, enabled = !uiState.isDeleting,
                colors = ButtonDefaults.buttonColors(containerColor = ClearanceDanger, contentColor = Color.White)
            ) {
                if (uiState.isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.width(16.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = viewModel::dismissDeleteDialog, enabled = !uiState.isDeleting) {
                Text("Cancel", color = ClearanceTextSecondary)
            }
        }
    )
}