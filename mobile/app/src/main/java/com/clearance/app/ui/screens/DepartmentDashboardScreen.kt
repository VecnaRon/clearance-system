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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clearance.app.data.api.dto.DepartmentQueueItemDto
import com.clearance.app.navigation.DepartmentNavActions
import com.clearance.app.ui.navigation.AppDrawerScaffold
import com.clearance.app.ui.navigation.DrawerItem
import com.clearance.app.ui.theme.ClearanceDanger
import com.clearance.app.ui.theme.ClearancePrimary
import com.clearance.app.ui.theme.ClearanceSuccess
import com.clearance.app.ui.theme.ClearanceSurface
import com.clearance.app.ui.theme.ClearanceTextMuted
import com.clearance.app.ui.theme.ClearanceTextPrimary
import com.clearance.app.ui.theme.ClearanceWarning
import com.clearance.app.viewmodel.DepartmentDashboardViewModel

fun departmentDrawerItems(nav: DepartmentNavActions): List<DrawerItem> = listOf(
    DrawerItem("Department", nav.onDepartment, isSelected = true),
    DrawerItem("Help", nav.onHelp)
)

@Composable
fun DepartmentDashboardScreen(
    viewModel: DepartmentDashboardViewModel = viewModel(),
    departmentNav: DepartmentNavActions,
    onOpenStudent: (Int) -> Unit
) {
    val uiState = viewModel.uiState

    AppDrawerScaffold(
        title = "Department Dashboard",
        drawerItems = departmentDrawerItems(departmentNav),
        onLogout = departmentNav.onLogout,
        topBarActions = {
            androidx.compose.material3.IconButton(onClick = viewModel::refresh) {
                Text(if (uiState.isRefreshing) "\u23F3" else "\u27F3", color = ClearancePrimary, fontSize = 18.sp)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) { CircularProgressIndicator(color = ClearancePrimary) }
                }
                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) { Text(text = uiState.errorMessage, color = ClearanceDanger, fontSize = 14.sp) }
                }
                else -> QueueContent(queue = uiState.queue, onOpenStudent = onOpenStudent)
            }
        }
    }
}

/**
 * UI/UX FINALIZATION: partitions the same `queue` list (unchanged
 * data/ViewModel) into "Needs Review" (status == pending) and
 * "Already Decided" (approved/rejected) sections, each its own
 * LazyColumn items() block with a section-prefixed key
 * ("pending-<id>" / "decided-<id>") — same precaution used for the
 * Admin Dashboard duplicate-key fix, applied here defensively even
 * though these two lists are a strict partition (never overlapping)
 * of the source list, so a collision isn't actually possible — but
 * the prefix costs nothing and keeps the pattern consistent.
 * Statistics (Total/Pending/Cleared/Rejected) use the exact same
 * `queue` counts as before, now laid out as a 2x2 grid.
 */
@Composable
private fun QueueContent(queue: List<DepartmentQueueItemDto>, onOpenStudent: (Int) -> Unit) {
    val total = queue.size
    val pending = queue.count { it.status == "pending" }
    val cleared = queue.count { it.status == "approved" }
    val rejected = queue.count { it.status == "rejected" }

    val needsReview = queue.filter { it.status == "pending" }
    val alreadyDecided = queue.filter { it.status == "approved" || it.status == "rejected" }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCard(label = "Total", value = total, color = ClearancePrimary, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(10.dp))
                    StatCard(label = "Pending", value = pending, color = ClearanceWarning, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatCard(label = "Cleared", value = cleared, color = ClearanceSuccess, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(10.dp))
                    StatCard(label = "Rejected", value = rejected, color = ClearanceDanger, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        item {
            SectionHeader(title = "Needs Review", subtitle = "Students awaiting your decision")
        }
        if (needsReview.isEmpty()) {
            item {
                Text(
                    text = "No students currently need review.",
                    color = ClearanceTextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
            }
        } else {
            items(needsReview, key = { "pending-${it.clearance_step_id}" }) { item ->
                QueueRow(item = item, actionLabel = "Review \u2192", onClick = { onOpenStudent(item.clearance_step_id) })
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }

        item {
            SectionHeader(title = "Already Decided", subtitle = "Previously reviewed \u2014 tap to reopen if needed")
        }
        if (alreadyDecided.isEmpty()) {
            item {
                Text(
                    text = "No decisions made yet.",
                    color = ClearanceTextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        } else {
            items(alreadyDecided, key = { "decided-${it.clearance_step_id}" }) { item ->
                QueueRow(item = item, actionLabel = "Review / Reopen \u2192", onClick = { onOpenStudent(item.clearance_step_id) })
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(text = title.uppercase(), color = ClearanceTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        Text(text = subtitle, color = ClearanceTextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun StatCard(label: String, value: Int, color: Color, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ClearanceSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value.toString(), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ClearanceTextMuted)
        }
    }
}

@Composable
private fun QueueRow(item: DepartmentQueueItemDto, actionLabel: String, onClick: () -> Unit) {
    val badgeColor = when (item.status) { "approved" -> ClearanceSuccess; "rejected" -> ClearanceDanger; else -> ClearanceWarning }

    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ClearanceSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.full_name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ClearanceTextPrimary)
                Text(text = item.admission_number, fontSize = 12.sp, color = ClearanceTextMuted)
                Text(
                    text = item.status.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = badgeColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .padding(top = 4.dp)
                )
            }
            TextButton(onClick = onClick) { Text(text = actionLabel, color = ClearancePrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
        }
    }
}