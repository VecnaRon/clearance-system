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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
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
import com.clearance.app.data.api.dto.AdminOverviewDto
import com.clearance.app.data.api.dto.AuditLogDto
import com.clearance.app.data.api.dto.PendingUserDto
import com.clearance.app.navigation.AdminNavActions
import com.clearance.app.ui.navigation.AppDrawerScaffold
import com.clearance.app.ui.navigation.DrawerItem
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
import com.clearance.app.viewmodel.AdminDashboardViewModel

fun adminDrawerItems(nav: AdminNavActions, selected: String): List<DrawerItem> = listOf(
    DrawerItem("Dashboard", nav.onDashboard, isSelected = selected == "Dashboard"),
    DrawerItem("Departments", nav.onDepartments, isSelected = selected == "Departments"),
    DrawerItem("Users", nav.onUsers, isSelected = selected == "Users"),
    DrawerItem("Reports", nav.onReports, isSelected = selected == "Reports"),
    DrawerItem("Audit Activity", nav.onAudit, isSelected = selected == "Audit Activity"),
    DrawerItem("Help", nav.onHelp, isSelected = selected == "Help")
)

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = viewModel(),
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
        title = "Admin Dashboard",
        drawerItems = adminDrawerItems(adminNav, selected = "Dashboard"),
        onLogout = adminNav.onLogout,
        topBarActions = {
            IconButton(onClick = viewModel::refresh) {
                Text(
                    text = if (uiState.isRefreshing) "\u23F3" else "\u27F3",
                    color = ClearancePrimary,
                    fontSize = 18.sp
                )
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
            DashboardBody(
                uiState = uiState,
                onActivate = viewModel::activateUser,
                onFinalApprove = viewModel::finalApprove,
                onViewAllAudit = adminNav.onAudit,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )
        }

        SnackbarHost(hostState = snackbarHostState) { data -> Snackbar(snackbarData = data) }
    }
}

@Composable
private fun DashboardBody(
    uiState: com.clearance.app.viewmodel.AdminDashboardUiState,
    onActivate: (Int) -> Unit,
    onFinalApprove: (Int) -> Unit,
    onViewAllAudit: () -> Unit,
    modifier: Modifier
) {
    val awaitingFinal = uiState.overview.filter { it.status == "awaiting_final" }

    LazyColumn(modifier = modifier.padding(16.dp)) {
        item { StatisticsRow(uiState.overview, uiState.pendingUsers) }
        item { Spacer(modifier = Modifier.height(16.dp)) }

        if (uiState.pendingUsers.isNotEmpty()) {
            item { SectionHeader("\uD83D\uDC64 Pending Account Activations (${uiState.pendingUsers.size})") }
            // CRASH FIX: key prefixed "pending-" — this LazyColumn also
            // renders clearance items (a different id space) below, so
            // the key must be unique across the WHOLE column, not just
            // within this items() call.
            items(uiState.pendingUsers, key = { "pending-${it.id}" }) { user ->
                PendingUserRow(user, uiState.activatingUserId, onActivate)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        if (awaitingFinal.isNotEmpty()) {
            item { SectionHeader("\uD83C\uDF93 Final Approvals Required (${awaitingFinal.size})") }
            // CRASH FIX (root cause): awaitingFinal is a SUBSET of
            // uiState.overview (same AdminOverviewDto.id values) —
            // rendering both lists' items with an unprefixed
            // `it.id` key in the same LazyColumn is what produced
            // "Key 3 was already used" for any awaiting_final clearance.
            items(awaitingFinal, key = { "awaiting-${it.id}" }) { item ->
                AwaitingFinalRow(item, uiState.approvingClearanceId, onFinalApprove)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        item { SectionHeader("All Clearance Requests (${uiState.overview.size})") }
        if (uiState.overview.isEmpty()) {
            item { Text(text = "No clearance requests found.", color = ClearanceTextMuted, fontSize = 14.sp) }
        } else {
            items(uiState.overview, key = { "clearance-${it.id}" }) { item -> ClearanceRow(item) }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }

        item { RecentActivitySummary(uiState.auditLogs, onViewAllAudit) }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text = text, color = ClearanceTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
private fun StatisticsRow(overview: List<AdminOverviewDto>, pendingUsers: List<PendingUserDto>) {
    val total = overview.size
    val pending = overview.count { it.status == "pending" }
    val approved = overview.count { it.status == "approved" }
    val awaitingFinal = overview.count { it.status == "awaiting_final" }
    val pendingActivations = pendingUsers.size

    val stats = listOf("Total" to total, "Pending" to pending, "Approved" to approved, "Awaiting Final" to awaitingFinal, "Pending Activations" to pendingActivations)

    LazyRow {
        items(stats.size) { index ->
            val (label, value) = stats[index]
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ClearanceSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.width(120.dp).padding(end = 8.dp)) {
                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = value.toString(), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = ClearancePrimary)
                    Text(text = label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = ClearanceTextMuted)
                }
            }
        }
    }
}

@Composable
private fun PendingUserRow(user: PendingUserDto, activatingUserId: Int?, onActivate: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(8.dp)).background(ClearanceGray100).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = user.full_name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ClearanceTextPrimary)
            Text(text = user.admission_number, fontSize = 12.sp, color = ClearanceTextMuted)
        }
        Button(onClick = { onActivate(user.id) }, enabled = activatingUserId == null, colors = ButtonDefaults.buttonColors(containerColor = ClearanceSuccess, contentColor = Color.White)) {
            if (activatingUserId == user.id) CircularProgressIndicator(modifier = Modifier.width(14.dp), strokeWidth = 2.dp, color = Color.White)
            else Text("Activate", fontSize = 13.sp)
        }
    }
}

@Composable
private fun AwaitingFinalRow(item: AdminOverviewDto, approvingClearanceId: Int?, onFinalApprove: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(8.dp)).background(ClearanceGray100).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.full_name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ClearanceTextPrimary)
            Text(text = "${item.cleared_count}/${item.total_departments} All Cleared", fontSize = 12.sp, color = ClearanceSuccess)
        }
        Button(onClick = { onFinalApprove(item.id) }, enabled = approvingClearanceId == null, colors = ButtonDefaults.buttonColors(containerColor = ClearanceSuccess, contentColor = Color.White)) {
            if (approvingClearanceId == item.id) CircularProgressIndicator(modifier = Modifier.width(14.dp), strokeWidth = 2.dp, color = Color.White)
            else Text("Approve", fontSize = 13.sp)
        }
    }
}

@Composable
private fun ClearanceRow(item: AdminOverviewDto) {
    val badgeColor = when (item.status) { "approved" -> ClearanceSuccess; "awaiting_final" -> ClearanceInfo; "rejected" -> ClearanceDanger; else -> ClearanceWarning }
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(8.dp)).background(ClearanceGray100).padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.full_name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ClearanceTextPrimary)
            Text(text = "${item.cleared_count}/${item.total_departments}", fontSize = 11.sp, color = ClearanceTextMuted)
        }
        Text(
            text = item.status.replace("_", " ").uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor,
            modifier = Modifier.clip(RoundedCornerShape(50)).background(badgeColor.copy(alpha = 0.15f)).padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun RecentActivitySummary(auditLogs: List<AuditLogDto>, onViewAll: () -> Unit) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ClearanceSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Recent Activity", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ClearanceTextPrimary)
            Text(text = "${auditLogs.size} recent action(s)", fontSize = 12.sp, color = ClearanceTextMuted, modifier = Modifier.padding(top = 2.dp, bottom = 8.dp))
            auditLogs.take(3).forEach { log -> Text(text = "${log.action ?: "\u2014"} \u2014 ${log.actor ?: "unknown"}", fontSize = 12.sp, color = ClearanceTextSecondary) }
            TextButton(onClick = onViewAll, modifier = Modifier.padding(top = 8.dp)) { Text("View All \u2192", color = ClearancePrimary, fontWeight = FontWeight.SemiBold) }
        }
    }
}