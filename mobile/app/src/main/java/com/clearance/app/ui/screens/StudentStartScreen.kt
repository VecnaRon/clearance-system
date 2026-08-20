package com.clearance.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clearance.app.navigation.StudentNavActions
import com.clearance.app.ui.navigation.AppDrawerScaffold
import com.clearance.app.ui.navigation.DrawerItem
import com.clearance.app.ui.theme.ClearanceAccent
import com.clearance.app.ui.theme.ClearanceDanger
import com.clearance.app.ui.theme.ClearanceGray100
import com.clearance.app.ui.theme.ClearanceGray700
import com.clearance.app.ui.theme.ClearanceInfo
import com.clearance.app.ui.theme.ClearancePrimary
import com.clearance.app.ui.theme.ClearanceSuccess
import com.clearance.app.ui.theme.ClearanceTextMuted
import com.clearance.app.ui.theme.ClearanceTextPrimary
import com.clearance.app.ui.theme.ClearanceTextSecondary
import com.clearance.app.ui.theme.ClearanceWarning
import com.clearance.app.viewmodel.ClearanceLifecycleState
import com.clearance.app.viewmodel.StudentHomeViewModel
import com.clearance.app.viewmodel.toLifecycleState

fun studentDrawerItems(nav: StudentNavActions, selected: String): List<DrawerItem> = listOf(
    DrawerItem("Start Clearance", nav.onStart, isSelected = selected == "Start"),
    DrawerItem("My Status", nav.onStatus, isSelected = selected == "Status"),
    DrawerItem("Downloads", nav.onDownloads, isSelected = selected == "Downloads"),
    DrawerItem("Help", nav.onHelp, isSelected = selected == "Help")
)

@Composable
fun StudentStartScreen(
    viewModel: StudentHomeViewModel = viewModel(),
    studentNav: StudentNavActions,
    onNavigateToForm: () -> Unit
) {
    val uiState = viewModel.uiState

    AppDrawerScaffold(
        title = "ClearanceSystem",
        drawerItems = studentDrawerItems(studentNav, selected = "Start"),
        onLogout = studentNav.onLogout
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ClearancePrimary)
                    }
                }
                uiState.loadError != null -> {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = uiState.loadError, color = ClearanceDanger, fontSize = 14.sp)
                    }
                }
                else -> {
                    when (uiState.clearance.toLifecycleState()) {
                        ClearanceLifecycleState.NO_CLEARANCE -> {
                            HeroSection()
                            Spacer(modifier = Modifier.height(16.dp))
                            ContentSection(onNavigateToForm)
                        }
                        ClearanceLifecycleState.PENDING -> {
                            val hasRejectedStep = uiState.clearance?.departments?.any { it.status == "rejected" } == true
                            StatusStateCard(
                                emoji = "\u23F3", title = "Clearance In Progress",
                                body = "Your clearance request is being reviewed by departments.",
                                color = ClearanceWarning, showRejectedNotice = hasRejectedStep,
                                actionLabel = "View Clearance Status", onAction = studentNav.onStatus
                            )
                        }
                        ClearanceLifecycleState.AWAITING_FINAL -> {
                            StatusStateCard(
                                emoji = "\uD83C\uDF93", title = "All Departments Cleared",
                                body = "Awaiting final approval from the administrator.",
                                color = ClearanceInfo, showRejectedNotice = false,
                                actionLabel = "View Clearance Status", onAction = studentNav.onStatus
                            )
                        }
                        ClearanceLifecycleState.APPROVED -> {
                            StatusStateCard(
                                emoji = "\u2705", title = "Clearance Approved",
                                body = "Your clearance is complete. You can now download your certificate.",
                                color = ClearanceSuccess, showRejectedNotice = false,
                                actionLabel = "View Clearance / Download Certificate", onAction = studentNav.onStatus
                            )
                        }
                        ClearanceLifecycleState.UNKNOWN -> {
                            StatusStateCard(
                                emoji = "\u2139\uFE0F", title = uiState.clearance?.status ?: "Unknown Status",
                                body = "View your clearance status for details.",
                                color = ClearanceTextMuted, showRejectedNotice = false,
                                actionLabel = "View Clearance Status", onAction = studentNav.onStatus
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusStateCard(emoji: String, title: String, body: String, color: Color, showRejectedNotice: Boolean, actionLabel: String, onAction: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 40.sp)
            Text(text = title, color = ClearanceTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
            Text(text = body, color = ClearanceTextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
            if (showRejectedNotice) {
                Text(
                    text = "\u26A0\uFE0F One or more departments have flagged this request. Check status for details.",
                    color = ClearanceDanger, fontSize = 12.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp).clip(RoundedCornerShape(8.dp)).background(ClearanceDanger.copy(alpha = 0.1f)).padding(10.dp)
                )
            }
            Button(onClick = onAction, colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White), shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(top = 20.dp)) {
                Text(actionLabel, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HeroSection() {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(colors = listOf(ClearancePrimary, ClearanceAccent), start = Offset(0f, 0f), end = Offset(1000f, 1000f))).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Start Your Digital Clearance Journey", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Welcome to the future of school clearance! Our streamlined digital process eliminates the hassle of running between departments and waiting in long queues.",
                color = Color.White, fontSize = 16.sp, lineHeight = 24.sp, textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ContentSection(onNavigateToForm: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "\uD83D\uDCCB How the Process Works", color = ClearanceTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            ProcessStep(1, "Submit Your Request:", "Fill out the comprehensive clearance form with your personal and academic details. Ensure all information is accurate and complete.")
            ProcessStep(2, "Automatic Distribution:", "Your request is instantly sent to all relevant departments including Library, Accounts, Sports, Hostel, and Academic offices.")
            ProcessStep(3, "Department Review:", "Each department reviews your clearance status, checks for outstanding dues, and provides their approval or feedback.")
            ProcessStep(4, "Real-time Tracking:", "Monitor your progress through our intuitive dashboard. Get instant notifications when departments update your status.")
            ProcessStep(5, "Final Approval & Certificate:", "Once all departments clear you, receive final approval and download your official clearance certificate.")
            Spacer(modifier = Modifier.height(8.dp))
            ImportantNote()
            Spacer(modifier = Modifier.height(20.dp))
            CallToAction(onNavigateToForm)
        }
    }
}

@Composable
private fun ProcessStep(number: Int, bold: String, rest: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(8.dp)).background(ClearanceGray100).padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(ClearancePrimary), contentAlignment = Alignment.Center) {
            Text(text = number.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = bold, color = ClearanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = rest, color = ClearanceGray700, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun ImportantNote() {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(ClearanceWarning.copy(alpha = 0.15f)).padding(16.dp)) {
        Text(text = "\u26A0\uFE0F Important Information", color = ClearanceWarning, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Before submitting: Ensure all your information is accurate and up-to-date. You may need to settle any outstanding dues with departments before receiving clearance. The process typically takes 3-5 business days depending on department response times.",
            color = ClearanceGray700, fontSize = 14.sp, lineHeight = 20.sp
        )
    }
}

@Composable
private fun CallToAction(onNavigateToForm: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(ClearanceGray100).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Ready to Begin?", color = ClearanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "Start your clearance request now and experience the convenience of our digital system.", color = ClearanceGray700, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToForm, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = ClearancePrimary, contentColor = Color.White)) {
            Text(text = "\uD83D\uDE80 Start Clearance Request", fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp))
        }
    }
}