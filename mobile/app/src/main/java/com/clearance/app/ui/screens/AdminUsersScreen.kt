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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clearance.app.data.api.dto.AdminUserDto
import com.clearance.app.ui.navigation.AdminNavActions
import com.clearance.app.ui.navigation.AppDrawerScaffold
import com.clearance.app.ui.theme.ClearanceDanger
import com.clearance.app.ui.theme.ClearanceDialogBackground
import com.clearance.app.ui.theme.ClearanceInputBackground
import com.clearance.app.ui.theme.ClearanceInputLabel
import com.clearance.app.ui.theme.ClearancePrimary
import com.clearance.app.ui.theme.ClearanceSnackbarBackground
import com.clearance.app.ui.theme.ClearanceSnackbarText
import com.clearance.app.ui.theme.ClearanceSuccess
import com.clearance.app.ui.theme.ClearanceSurface
import com.clearance.app.ui.theme.ClearanceTextMuted
import com.clearance.app.ui.theme.ClearanceTextPrimary
import com.clearance.app.ui.theme.ClearanceTextSecondary
import com.clearance.app.ui.theme.ClearanceWarning
import com.clearance.app.viewmodel.AdminUsersViewModel
import com.google.gson.JsonElement

private val ROLE_OPTIONS = listOf(
    "hod" to "Head of Department",
    "admin" to "Administrator",
    "student" to "Student",
    "staff" to "Staff"
)

private fun JsonElement?.isActiveTrue(): Boolean {
    if (this == null) return false

    return try {
        when {
            this.isJsonPrimitive && this.asJsonPrimitive.isBoolean -> this.asBoolean
            this.isJsonPrimitive && this.asJsonPrimitive.isNumber -> this.asInt == 1
            else -> false
        }
    } catch (e: Exception) {
        false
    }
}

/**
 * NOTE (Part 3 disclosure): GET /api/admin/users only returns
 * is_active=1 rows (confirmed in admin.service.js). This screen
 * therefore only ever shows ACTIVE users — deactivated non-student
 * accounts are currently invisible to this endpoint entirely. Search
 * bar / role+status filters are deliberately NOT built yet, pending
 * your decision on how to handle this backend limitation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    viewModel: AdminUsersViewModel = viewModel(),
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
        title = "Manage Users",
        drawerItems = adminDrawerItems(
            adminNav,
            selected = "Users"
        ),
        onLogout = adminNav.onLogout
    ) { innerPadding ->

        if (uiState.isLoading) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = ClearancePrimary
                )
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
                    text = "Showing active users only (backend limitation — see Part 3 disclosure).",
                    fontSize = 12.sp,
                    color = ClearanceTextMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TextButton(
                    onClick = viewModel::refresh,
                    enabled = !uiState.isRefreshing
                ) {
                    Text(
                        if (uiState.isRefreshing) {
                            "Refreshing..."
                        } else {
                            "\u27F3 Refresh"
                        },
                        color = ClearancePrimary
                    )
                }

                UserFormCard(viewModel = viewModel)

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "User Management (${uiState.users.size})",
                    color = ClearanceTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                if (uiState.users.isEmpty()) {

                    Text(
                        text = "No users found.",
                        color = ClearanceTextMuted,
                        fontSize = 14.sp
                    )

                } else {

                    uiState.users.forEach { user ->

                        UserRow(
                            user = user,
                            mutatingUserId = uiState.mutatingUserId,
                            onEdit = {
                                viewModel.startEdit(user)
                            },
                            onActivate = {
                                viewModel.activateUser(user.id)
                            },
                            onDeactivate = {
                                viewModel.requestDeactivate(user)
                            },
                            onDelete = {
                                viewModel.requestDelete(user)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState
        ) { data ->

            Snackbar(
                snackbarData = data,
                containerColor = ClearanceSnackbarBackground,
                contentColor = ClearanceSnackbarText
            )
        }
    }

    if (uiState.deletingUser != null) {
        DeleteUserDialog(viewModel)
    }

    if (uiState.deactivatingUser != null) {
        DeactivateUserDialog(viewModel)
    }
}

@Composable
private fun clearanceFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = ClearanceTextPrimary,
    unfocusedTextColor = ClearanceTextPrimary,
    disabledTextColor = ClearanceTextMuted,
    focusedLabelColor = ClearancePrimary,
    unfocusedLabelColor = ClearanceInputLabel,
    focusedBorderColor = ClearancePrimary,
    unfocusedBorderColor = ClearanceTextMuted,
    focusedContainerColor = ClearanceInputBackground,
    unfocusedContainerColor = ClearanceInputBackground,
    cursorColor = ClearancePrimary
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserFormCard(
    viewModel: AdminUsersViewModel
) {
    val uiState = viewModel.uiState
    val form = uiState.formState
    val isEditing = form.editingId != null
    val fieldColors = clearanceFieldColors()

    var roleExpanded by remember {
        mutableStateOf(false)
    }

    var deptExpanded by remember {
        mutableStateOf(false)
    }

    val roleLabel =
        ROLE_OPTIONS.firstOrNull {
            it.first == form.role
        }?.second ?: form.role

    val deptLabel =
        if (form.department.isBlank()) {
            "\u2014 No Department \u2014"
        } else {
            form.department
        }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ClearanceSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = if (isEditing) {
                    "\u270F\uFE0F Edit User"
                } else {
                    "\u2795 Add New User"
                },
                color = ClearanceTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = form.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = {
                    Text("Full Name")
                },
                singleLine = true,
                enabled = !uiState.isSavingForm,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.email,
                onValueChange = viewModel::onEmailChange,
                label = {
                    Text("Email Address")
                },
                singleLine = true,
                enabled = !uiState.isSavingForm,
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )

            OutlinedTextField(
                value = form.username,
                onValueChange = viewModel::onUsernameChange,
                label = {
                    Text("Username")
                },
                singleLine = true,
                enabled = !uiState.isSavingForm,
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )

            OutlinedTextField(
                value = form.password,
                onValueChange = viewModel::onPasswordChange,
                label = {
                    Text(
                        if (isEditing) {
                            "Password (leave blank to keep current)"
                        } else {
                            "Password"
                        }
                    )
                },
                singleLine = true,
                enabled = !uiState.isSavingForm,
                colors = fieldColors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )

            Text(
                text = "User Role",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClearanceTextMuted,
                modifier = Modifier.padding(
                    top = 14.dp,
                    bottom = 4.dp
                )
            )

            ExposedDropdownMenuBox(
                expanded = roleExpanded,
                onExpandedChange = {
                    roleExpanded = it
                }
            ) {

                OutlinedTextField(
                    value = roleLabel,
                    onValueChange = {},
                    readOnly = true,
                    colors = fieldColors,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = roleExpanded
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            MenuAnchorType.PrimaryNotEditable
                        )
                )

                DropdownMenu(
                    expanded = roleExpanded,
                    onDismissRequest = {
                        roleExpanded = false
                    }
                ) {

                    ROLE_OPTIONS.forEach { (value, label) ->

                        DropdownMenuItem(
                            text = {
                                Text(label)
                            },
                            onClick = {
                                viewModel.onRoleChange(value)
                                roleExpanded = false
                            }
                        )
                    }
                }
            }

            Text(
                text = "Department (if HOD/Staff)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ClearanceTextMuted,
                modifier = Modifier.padding(
                    top = 14.dp,
                    bottom = 4.dp
                )
            )

            ExposedDropdownMenuBox(
                expanded = deptExpanded,
                onExpandedChange = {
                    deptExpanded = it
                }
            ) {

                OutlinedTextField(
                    value = deptLabel,
                    onValueChange = {},
                    readOnly = true,
                    colors = fieldColors,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = deptExpanded
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(
                            MenuAnchorType.PrimaryNotEditable
                        )
                )

                DropdownMenu(
                    expanded = deptExpanded,
                    onDismissRequest = {
                        deptExpanded = false
                    }
                ) {

                    DropdownMenuItem(
                        text = {
                            Text("\u2014 No Department \u2014")
                        },
                        onClick = {
                            viewModel.onDepartmentChange("")
                            deptExpanded = false
                        }
                    )

                    uiState.departments.forEach { dept ->

                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (!dept.code.isNullOrBlank()) {
                                        "${dept.name} (${dept.code})"
                                    } else {
                                        dept.name
                                    }
                                )
                            },
                            onClick = {
                                viewModel.onDepartmentChange(dept.name)
                                deptExpanded = false
                            }
                        )
                    }
                }
            }

            if (uiState.formError != null) {

                Text(
                    text = uiState.formError,
                    color = ClearanceDanger,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            Row(
                modifier = Modifier.padding(top = 16.dp)
            ) {

                Button(
                    onClick = viewModel::submitForm,
                    enabled = !uiState.isSavingForm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClearancePrimary,
                        contentColor = Color.White
                    )
                ) {

                    if (uiState.isSavingForm) {

                        CircularProgressIndicator(
                            modifier = Modifier.width(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )

                    } else {

                        Text(
                            if (isEditing) {
                                "\uD83D\uDCBE Update User"
                            } else {
                                "\u2795 Create User"
                            }
                        )
                    }
                }

                if (isEditing) {

                    TextButton(
                        onClick = viewModel::cancelEdit,
                        enabled = !uiState.isSavingForm
                    ) {
                        Text(
                            "\u274C Cancel",
                            color = ClearanceTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserRow(
    user: AdminUserDto,
    mutatingUserId: Int?,
    onEdit: () -> Unit,
    onActivate: () -> Unit,
    onDeactivate: () -> Unit,
    onDelete: () -> Unit
) {
    val isActive = user.is_active.isActiveTrue()

    val roleColor = when (user.role) {
        "admin" -> ClearanceDanger
        "hod" -> ClearancePrimary
        else -> ClearanceSuccess
    }

    val isMutatingThis =
        mutatingUserId == user.id

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = ClearanceSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {

        Column(
            modifier = Modifier.padding(14.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = user.full_name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ClearanceTextPrimary
                    )

                    Text(
                        text = user.email,
                        fontSize = 12.sp,
                        color = ClearanceTextMuted
                    )
                }

                Text(
                    text = user.role.uppercase(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(roleColor)
                        .padding(
                            horizontal = 8.dp,
                            vertical = 3.dp
                        )
                )
            }

            Text(
                text = if (user.department.isNotBlank()) {
                    "Dept: ${user.department}"
                } else {
                    "Dept: \u2014"
                },
                fontSize = 12.sp,
                color = ClearanceTextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = if (isActive) {
                    "Active"
                } else {
                    "Inactive"
                },
                color = if (isActive) {
                    ClearanceSuccess
                } else {
                    ClearanceDanger
                },
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                modifier = Modifier.padding(top = 10.dp)
            ) {

                TextButton(
                    onClick = onEdit,
                    enabled = mutatingUserId == null
                ) {
                    Text(
                        "\u270F\uFE0F Edit",
                        color = ClearancePrimary,
                        fontSize = 13.sp
                    )
                }

                if (user.role != "admin") {

                    if (!isActive) {

                        TextButton(
                            onClick = onActivate,
                            enabled = mutatingUserId == null
                        ) {

                            if (isMutatingThis) {

                                CircularProgressIndicator(
                                    modifier = Modifier.width(14.dp),
                                    strokeWidth = 2.dp,
                                    color = ClearanceSuccess
                                )

                            } else {

                                Text(
                                    "\u2705 Activate",
                                    color = ClearanceSuccess,
                                    fontSize = 13.sp
                                )
                            }
                        }

                    } else {

                        TextButton(
                            onClick = onDeactivate,
                            enabled = mutatingUserId == null
                        ) {
                            Text(
                                "\uD83D\uDD12 Deactivate",
                                color = ClearanceWarning,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                TextButton(
                    onClick = onDelete,
                    enabled = mutatingUserId == null
                ) {
                    Text(
                        "\uD83D\uDDD1\uFE0F Delete",
                        color = ClearanceDanger,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteUserDialog(
    viewModel: AdminUsersViewModel
) {
    val uiState = viewModel.uiState
    val target = uiState.deletingUser ?: return

    AlertDialog(
        onDismissRequest = viewModel::dismissDeleteDialog,
        containerColor = ClearanceDialogBackground,
        titleContentColor = ClearanceTextPrimary,
        textContentColor = ClearanceTextSecondary,

        title = {
            Text(
                "Delete User?",
                fontWeight = FontWeight.Bold,
                color = ClearanceTextPrimary
            )
        },

        text = {
            Text(
                "This permanently removes \"${target.full_name}\"'s account AND their associated clearance records. This action cannot be undone.",
                color = ClearanceTextSecondary
            )
        },

        confirmButton = {

            Button(
                onClick = viewModel::confirmDelete,
                enabled = !uiState.isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClearanceDanger,
                    contentColor = Color.White
                )
            ) {

                if (uiState.isDeleting) {

                    CircularProgressIndicator(
                        modifier = Modifier.width(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )

                } else {

                    Text("Delete Permanently")
                }
            }
        },

        dismissButton = {

            TextButton(
                onClick = viewModel::dismissDeleteDialog,
                enabled = !uiState.isDeleting
            ) {
                Text(
                    "Cancel",
                    color = ClearanceTextSecondary
                )
            }
        }
    )
}

@Composable
private fun DeactivateUserDialog(
    viewModel: AdminUsersViewModel
) {
    val uiState = viewModel.uiState
    val target = uiState.deactivatingUser ?: return

    AlertDialog(
        onDismissRequest = viewModel::dismissDeactivateDialog,
        containerColor = ClearanceDialogBackground,
        titleContentColor = ClearanceTextPrimary,
        textContentColor = ClearanceTextSecondary,

        title = {
            Text(
                "Deactivate Account?",
                fontWeight = FontWeight.Bold,
                color = ClearanceTextPrimary
            )
        },

        text = {
            Text(
                "Deactivate account for \"${target.full_name}\"? They will not be able to log in.",
                color = ClearanceTextSecondary
            )
        },

        confirmButton = {

            Button(
                onClick = viewModel::confirmDeactivate,
                enabled = !uiState.isDeactivating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClearanceWarning,
                    contentColor = Color.White
                )
            ) {

                if (uiState.isDeactivating) {

                    CircularProgressIndicator(
                        modifier = Modifier.width(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )

                } else {

                    Text("Deactivate")
                }
            }
        },

        dismissButton = {

            TextButton(
                onClick = viewModel::dismissDeactivateDialog,
                enabled = !uiState.isDeactivating
            ) {
                Text(
                    "Cancel",
                    color = ClearanceTextSecondary
                )
            }
        }
    )
}