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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clearance.app.data.api.dto.DepartmentReportDto
import com.clearance.app.ui.navigation.AdminNavActions
import com.clearance.app.ui.navigation.AppDrawerScaffold
import com.clearance.app.ui.theme.ClearanceAccent
import com.clearance.app.ui.theme.ClearanceDanger
import com.clearance.app.ui.theme.ClearancePrimary
import com.clearance.app.ui.theme.ClearanceSuccess
import com.clearance.app.ui.theme.ClearanceSurface
import com.clearance.app.ui.theme.ClearanceTextMuted
import com.clearance.app.ui.theme.ClearanceTextPrimary
import com.clearance.app.ui.theme.ClearanceWarning
import com.clearance.app.viewmodel.AdminReportsViewModel
import com.google.gson.JsonElement

private fun JsonElement?.toIntSafe(): Int {
    if (this == null) return 0
    return try { this.asInt } catch (e: Exception) { 0 }
}

@Composable
fun AdminReportsScreen(
    viewModel: AdminReportsViewModel = viewModel(),
    adminNav: AdminNavActions
) {
    val uiState = viewModel.uiState

    AppDrawerScaffold(
        title = "Clearance Reports",
        drawerItems = adminDrawerItems(adminNav, selected = "Reports"),
        onLogout = adminNav.onLogout
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) { CircularProgressIndicator(color = ClearancePrimary) }
        } else if (uiState.errorMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) { Text(text = uiState.errorMessage, color = ClearanceDanger, fontSize = 14.sp) }
        } else {
            ReportContent(rows = uiState.rows, modifier = Modifier.fillMaxSize().padding(innerPadding))
        }
    }
}

@Composable
private fun ReportContent(rows: List<DepartmentReportDto>, modifier: Modifier) {
    val totalCleared = rows.sumOf { it.cleared.toIntSafe() }
    val totalRejected = rows.sumOf { it.rejected.toIntSafe() }
    val totalPending = rows.sumOf { it.pending.toIntSafe() }
    val totalRequests = totalCleared + totalRejected + totalPending

    Column(modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(colors = listOf(ClearancePrimary, ClearanceAccent), start = Offset(0f, 0f), end = Offset(1000f, 1000f))).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "\uD83D\uDCCA Clearance Reports", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = "Comprehensive overview of clearance statistics across all departments", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            SummaryCard("Total", totalRequests, ClearancePrimary, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            SummaryCard("Cleared", totalCleared, ClearanceSuccess, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            SummaryCard("Pending", totalPending, ClearanceWarning, Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            SummaryCard("Rejected", totalRejected, ClearanceDanger, Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Department-wise Clearance Summary", color = ClearanceTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
        if (rows.isEmpty()) {
            Text(text = "No report data available.", color = ClearanceTextMuted, fontSize = 14.sp)
        } else {
            rows.forEach { row -> DepartmentReportRow(row) }
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: Int, color: Color, modifier: Modifier) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ClearanceSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = value.toString(), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(text = label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ClearanceTextMuted)
        }
    }
}

@Composable
private fun DepartmentReportRow(row: DepartmentReportDto) {
    val cleared = row.cleared.toIntSafe(); val rejected = row.rejected.toIntSafe(); val pending = row.pending.toIntSafe()
    val total = cleared + rejected + pending

    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ClearanceSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(text = row.department_name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ClearanceTextPrimary)
                    if (!row.department_code.isNullOrBlank()) Text(text = row.department_code, fontSize = 11.sp, color = ClearanceTextMuted)
                }
                Text(text = total.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ClearanceTextPrimary)
            }
            Row(modifier = Modifier.padding(top = 8.dp)) {
                StatChip("Cleared", cleared, ClearanceSuccess); Spacer(modifier = Modifier.width(8.dp))
                StatChip("Rejected", rejected, ClearanceDanger); Spacer(modifier = Modifier.width(8.dp))
                StatChip("Pending", pending, ClearanceWarning)
            }
        }
    }
}

@Composable
private fun StatChip(label: String, value: Int, color: Color) {
    Column(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(text = value.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 10.sp, color = ClearanceTextMuted)
    }
}