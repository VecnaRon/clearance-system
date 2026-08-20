package com.clearance.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clearance.app.data.api.dto.AuditLogDto
import com.clearance.app.navigation.AdminNavActions
import com.clearance.app.ui.navigation.AppDrawerScaffold
import com.clearance.app.ui.theme.ClearanceDanger
import com.clearance.app.ui.theme.ClearancePrimary
import com.clearance.app.ui.theme.ClearanceSurface
import com.clearance.app.ui.theme.ClearanceTextMuted
import com.clearance.app.ui.theme.ClearanceTextPrimary
import com.clearance.app.ui.theme.ClearanceTextSecondary
import com.clearance.app.viewmodel.AdminAuditViewModel

@Composable
fun AdminAuditScreen(
    viewModel: AdminAuditViewModel = viewModel(),
    adminNav: AdminNavActions
) {
    val uiState = viewModel.uiState

    AppDrawerScaffold(
        title = "Audit Activity",
        drawerItems = adminDrawerItems(adminNav, selected = "Audit Activity"),
        onLogout = adminNav.onLogout,
        topBarActions = {
            IconButton(onClick = viewModel::refresh) {
                Text("\u27F3", color = ClearancePrimary, fontSize = 18.sp)
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
                        text = "Loading audit activity...",
                        color = ClearanceTextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = uiState.errorMessage, color = ClearanceDanger, fontSize = 14.sp)
                }
            }
            else -> {
                AuditList(logs = uiState.logs, innerPadding = innerPadding)
            }
        }
    }
}

@Composable
private fun AuditList(logs: List<AuditLogDto>, innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "Recent System Activity",
                color = ClearanceTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        }
        if (logs.isEmpty()) {
            item {
                Text(text = "No activity recorded yet", color = ClearanceTextMuted, fontSize = 14.sp)
            }
        } else {
            items(logs, key = { it.id }) { log ->
                AuditRow(log)
            }
        }
    }
}

@Composable
private fun AuditRow(log: AuditLogDto) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ClearanceSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = log.action ?: "\u2014", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ClearanceTextPrimary)
            Text(text = log.details ?: "\u2014", fontSize = 12.sp, color = ClearanceTextSecondary)
            Text(
                text = "${log.actor ?: "unknown"} \u2022 ${log.timestamp ?: "\u2014"}",
                fontSize = 11.sp,
                color = ClearanceTextMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}