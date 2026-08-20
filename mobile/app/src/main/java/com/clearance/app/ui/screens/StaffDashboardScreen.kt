package com.clearance.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clearance.app.data.api.dto.AdminUserDto
import com.clearance.app.data.api.dto.RecordDto
import com.clearance.app.navigation.StaffNavActions
import com.clearance.app.ui.navigation.AppDrawerScaffold
import com.clearance.app.ui.navigation.DrawerItem
import com.clearance.app.ui.theme.ClearanceAccent
import com.clearance.app.ui.theme.ClearanceDanger
import com.clearance.app.ui.theme.ClearanceGray100
import com.clearance.app.ui.theme.ClearanceInputBackground
import com.clearance.app.ui.theme.ClearancePrimary
import com.clearance.app.ui.theme.ClearanceSuccess
import com.clearance.app.ui.theme.ClearanceSurface
import com.clearance.app.ui.theme.ClearanceTextMuted
import com.clearance.app.ui.theme.ClearanceTextPrimary
import com.clearance.app.ui.theme.ClearanceTextSecondary
import com.clearance.app.viewmodel.StaffDashboardViewModel

fun staffDrawerItems(nav: StaffNavActions): List<DrawerItem> = listOf(
    DrawerItem("Dashboard", nav.onDashboard, isSelected = true),
    DrawerItem("Help", nav.onHelp)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffDashboardScreen(
    viewModel: StaffDashboardViewModel = viewModel(),
    staffNav: StaffNavActions
) {
    val uiState = viewModel.uiState

    AppDrawerScaffold(
        title = "Staff Dashboard",
        drawerItems = staffDrawerItems(staffNav),
        onLogout = staffNav.onLogout
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            HeaderCard(uiState.me?.full_name, uiState.me?.department_name)
            Spacer(modifier = Modifier.height(16.dp))
            StudentsSection(viewModel)
            Spacer(modifier = Modifier.height(16.dp))
            MyLogsSection(viewModel)
        }
    }
}

@Composable
private fun HeaderCard(fullName: String?, departmentName: String?) {
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
                .padding(24.dp)
        ) {
            Text(
                text = "Staff Dashboard",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = if (fullName != null) {
                    "Welcome, $fullName — ${departmentName ?: "Department not assigned"}"
                } else {
                    "Loading your profile..."
                },
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun StudentsSection(viewModel: StaffDashboardViewModel) {
    val uiState = viewModel.uiState

    val filteredStudents = uiState.students.filter {
        if (uiState.searchQuery.isBlank()) {
            true
        } else {
            val term = uiState.searchQuery.lowercase()
            it.full_name.lowercase().contains(term) ||
                    it.admission_number.lowercase().contains(term)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ClearanceSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                text = "👥 Students",
                color = ClearanceTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = {
                    Text("Filter by name or admission number...")
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ClearanceTextPrimary,
                    unfocusedTextColor = ClearanceTextPrimary,
                    focusedBorderColor = ClearancePrimary,
                    unfocusedBorderColor = ClearanceTextMuted,
                    focusedContainerColor = ClearanceInputBackground,
                    unfocusedContainerColor = ClearanceInputBackground,
                    cursorColor = ClearancePrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            filteredStudents.forEach { student ->
                StudentResultRow(
                    student = student,
                    isSelected = uiState.selectedStudent?.id == student.id,
                    onClick = {
                        viewModel.selectStudent(student)
                    }
                )
            }

            uiState.selectedStudent?.let { selected ->
                Spacer(modifier = Modifier.height(12.dp))
                StudentLogPanel(viewModel, selected)
            }

            uiState.logMessage?.let { feedback ->
                Text(
                    text = feedback.text,
                    color = if (feedback.success) {
                        ClearanceSuccess
                    } else {
                        ClearanceDanger
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            (if (feedback.success) {
                                ClearanceSuccess
                            } else {
                                ClearanceDanger
                            }).copy(alpha = 0.1f)
                        )
                        .padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun StudentResultRow(
    student: AdminUserDto,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) {
                    ClearancePrimary.copy(alpha = 0.08f)
                } else {
                    ClearanceGray100
                }
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Text(
            text = student.full_name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ClearanceTextPrimary
        )

        Text(
            text = "Adm: ${student.admission_number} | Form: ${student.class_form ?: "N/A"} | Stream: ${student.stream ?: "N/A"}",
            fontSize = 12.sp,
            color = ClearanceTextMuted
        )

        if (isSelected) {
            Text(
                text = "Selected",
                color = ClearancePrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun StudentLogPanel(
    viewModel: StaffDashboardViewModel,
    student: AdminUserDto
) {
    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ClearanceGray100)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Selected: ${student.full_name} (${student.admission_number})",
                color = ClearancePrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            TextButton(
                onClick = viewModel::closeSelectedStudent
            ) {
                Text(
                    "Close",
                    color = ClearancePrimary,
                    fontSize = 12.sp
                )
            }
        }

        Text(
            text = "Existing Records for this Student",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ClearanceTextMuted,
            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
        )

        when {
            uiState.loadingRecords -> {
                Text(
                    "Loading records...",
                    color = ClearanceTextMuted,
                    fontSize = 13.sp
                )
            }

            uiState.studentRecords.isEmpty() -> {
                Text(
                    "No records logged yet for this student.",
                    color = ClearanceTextMuted,
                    fontSize = 13.sp
                )
            }

            else -> {
                val totalOutstanding = uiState.studentRecords
                    .filter {
                        it.status == "unresolved" && it.amount != null
                    }
                    .sumOf {
                        it.amount ?: 0.0
                    }

                uiState.studentRecords.forEach { record ->
                    RecordRow(
                        record,
                        uiState.resolvingId,
                        viewModel::resolveRecord
                    )
                }

                Text(
                    "Total Outstanding: KES $totalOutstanding",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = ClearanceTextPrimary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Text(
            text = "Log a New Outstanding Item",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ClearanceTextMuted,
            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::onDescriptionChange,
            placeholder = {
                Text("e.g. Unreturned library book — Introduction to Java")
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ClearanceTextPrimary,
                unfocusedTextColor = ClearanceTextPrimary,
                focusedBorderColor = ClearancePrimary,
                unfocusedBorderColor = ClearanceTextMuted,
                focusedContainerColor = ClearanceInputBackground,
                unfocusedContainerColor = ClearanceInputBackground,
                cursorColor = ClearancePrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Text(
            text = "Amount (KES) — optional",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ClearanceTextMuted,
            modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)
        )

        OutlinedTextField(
            value = uiState.amount,
            onValueChange = viewModel::onAmountChange,
            placeholder = {
                Text("e.g. 3500")
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ClearanceTextPrimary,
                unfocusedTextColor = ClearanceTextPrimary,
                focusedBorderColor = ClearancePrimary,
                unfocusedBorderColor = ClearanceTextMuted,
                focusedContainerColor = ClearanceInputBackground,
                unfocusedContainerColor = ClearanceInputBackground,
                cursorColor = ClearancePrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = viewModel::logRecord,
            enabled = !uiState.submitting && uiState.description.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = ClearancePrimary,
                contentColor = Color.White
            ),
            modifier = Modifier.padding(top = 14.dp)
        ) {
            if (uiState.submitting) {
                CircularProgressIndicator(
                    modifier = Modifier.width(16.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text("Log Record")
            }
        }
    }
}

@Composable
private fun RecordRow(
    record: RecordDto,
    resolvingId: Int?,
    onResolve: (Int) -> Unit
) {
    val resolved = record.status == "resolved"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(ClearanceSurface)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = record.studentName ?: "Unknown",
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = ClearanceTextPrimary
            )

            Text(
                text = record.status.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (resolved) {
                    ClearanceSuccess
                } else {
                    ClearanceDanger
                }
            )
        }

        Text(
            text = record.description ?: "",
            fontSize = 12.sp,
            color = ClearanceTextSecondary
        )

        Text(
            text = if (record.amount != null) {
                "KES ${record.amount}"
            } else {
                "—"
            },
            fontSize = 11.sp,
            color = ClearanceTextMuted
        )

        if (!resolved) {
            TextButton(
                onClick = {
                    onResolve(record.id)
                },
                enabled = resolvingId == null
            ) {
                Text(
                    if (resolvingId == record.id) {
                        "Saving..."
                    } else {
                        "Mark Resolved"
                    },
                    color = ClearanceSuccess,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MyLogsSection(viewModel: StaffDashboardViewModel) {
    val uiState = viewModel.uiState
    var filterExpanded by remember {
        mutableStateOf(false)
    }

    val filterLabel = when (uiState.filterStatus) {
        "unresolved" -> "Unresolved Only"
        "resolved" -> "Resolved Only"
        else -> "All Records"
    }

    val filteredLogs = uiState.myLogs.filter {
        uiState.filterStatus == "all" ||
                it.status == uiState.filterStatus
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ClearanceSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                text = "📂 My Logged Records",
                color = ClearanceTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            ExposedDropdownMenuBox(
                expanded = filterExpanded,
                onExpandedChange = {
                    filterExpanded = it
                }
            ) {
                OutlinedTextField(
                    value = filterLabel,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = filterExpanded
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )

                DropdownMenu(
                    expanded = filterExpanded,
                    onDismissRequest = {
                        filterExpanded = false
                    }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text("All Records")
                        },
                        onClick = {
                            viewModel.onFilterStatusChange("all")
                            filterExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Unresolved Only")
                        },
                        onClick = {
                            viewModel.onFilterStatusChange("unresolved")
                            filterExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            Text("Resolved Only")
                        },
                        onClick = {
                            viewModel.onFilterStatusChange("resolved")
                            filterExpanded = false
                        }
                    )
                }
            }

            Text(
                text = "${filteredLogs.size} record(s)",
                fontSize = 12.sp,
                color = ClearanceTextMuted,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )

            when {
                uiState.logsLoading -> {
                    CircularProgressIndicator(
                        color = ClearancePrimary
                    )
                }

                filteredLogs.isEmpty() -> {
                    Text(
                        "No records found.",
                        color = ClearanceTextMuted,
                        fontSize = 14.sp
                    )
                }

                else -> {
                    filteredLogs.forEach { record ->
                        RecordRow(
                            record,
                            uiState.resolvingId,
                            viewModel::resolveRecord
                        )
                    }
                }
            }
        }
    }
}