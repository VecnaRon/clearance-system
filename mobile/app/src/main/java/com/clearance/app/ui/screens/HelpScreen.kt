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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.clearance.app.ui.theme.ClearanceDark
import com.clearance.app.ui.theme.ClearanceGray700
import com.clearance.app.ui.theme.ClearancePrimary
import com.clearance.app.ui.theme.ClearanceSurface
import com.clearance.app.ui.theme.ClearanceTextMuted
import com.clearance.app.ui.theme.ClearanceTextPrimary

private data class HelpStep(val title: String, val body: String)
private data class HelpGuide(val icon: String, val title: String, val steps: List<HelpStep>)

/** Content copied verbatim from client/src/pages/help/Help.js's `content` object. */
private val HELP_CONTENT: Map<String, HelpGuide> = mapOf(
    "student" to HelpGuide("\uD83C\uDF93", "Student Guide", listOf(
        HelpStep("Register Your Account", "Go to the Register page and fill in your details including your admission number. Your account will be inactive until the admin approves it."),
        HelpStep("Wait for Account Activation", "Once registered, wait for the administrator to activate your account. You will not be able to log in until this is done."),
        HelpStep("Log In", "Once activated, log in using your email and password on the Login page."),
        HelpStep("Start Your Clearance", "Navigate to Start Clearance and submit your clearance request. You can only submit once."),
        HelpStep("Track Your Progress", "Visit the Clearance Status page to see which departments have cleared you and which are still pending."),
        HelpStep("Resolve Outstanding Items", "If a department has flagged outstanding items against you such as unreturned books or unpaid fees, resolve them directly with the relevant department staff."),
        HelpStep("Download Your Certificate", "Once all departments have cleared you and the admin gives final approval, go to the Downloads page to download your clearance certificate.")
    )),
    "staff" to HelpGuide("\uD83D\uDC68\u200D\uD83D\uDCBC", "Staff Guide", listOf(
        HelpStep("Log In", "Use your email and password on the Login page. You will be directed to the Staff Dashboard automatically."),
        HelpStep("Find a Student", "On your dashboard you will see a full list of all registered students. Use the search bar to filter by name or admission number."),
        HelpStep("Log an Outstanding Item", "Click on a student to open their panel. Enter a description of the outstanding item such as an unreturned book or unpaid fee, add an amount if applicable, then click Log Record."),
        HelpStep("View Existing Records", "When you click a student you will see all records previously logged against them in your department, including their current status."),
        HelpStep("Mark Items as Resolved", "Once a student has settled an outstanding item, find the record and click Mark Resolved. This updates the record and notifies the HOD during their review."),
        HelpStep("View Your Logged Records", "The My Logged Records section at the bottom of your dashboard shows all records you have ever logged with filters for resolved and unresolved.")
    )),
    "hod" to HelpGuide("\uD83C\uDFDB\uFE0F", "Head of Department Guide", listOf(
        HelpStep("Log In", "Use your email and password on the Login page. You will be directed to the Department Dashboard automatically."),
        HelpStep("View the Student Queue", "Your dashboard shows all students who have submitted clearance requests for your department along with their current status."),
        HelpStep("Review a Student", "Click Review Student next to any student to open their full clearance review page."),
        HelpStep("Check Outstanding Records", "On the review page you will see all outstanding items logged by your department staff against that student. Review these carefully before making a decision."),
        HelpStep("Approve or Reject", "If all items are resolved, mark the student as Cleared. If there are unresolved items, mark as Rejected and add a remark explaining why. You can also save as Pending if you need more time."),
        HelpStep("Change a Decision", "You can change your decision at any time by reopening the student review. If you reject after previously approving, the overall clearance status will automatically revert.")
    )),
    "admin" to HelpGuide("\u2699\uFE0F", "Administrator Guide", listOf(
        HelpStep("Log In", "Use your admin email and password on the Login page. You will be directed to the Admin Dashboard."),
        HelpStep("Activate Student Accounts", "Newly registered students appear under Pending Activations on your dashboard. Review and activate legitimate accounts before they can log in."),
        HelpStep("Manage Departments", "Go to Manage Departments to add, edit, or deactivate departments in the system."),
        HelpStep("Manage Users", "Go to Manage Users to create HOD and staff accounts, assign them to departments, or deactivate any user account."),
        HelpStep("Give Final Approval", "When all departments have cleared a student the clearance appears as Awaiting Final Approval on your dashboard. Review and give final approval to allow the student to download their certificate."),
        HelpStep("View Reports", "The Reports page shows a department-wise summary of clearance progress across all students."),
        HelpStep("View Audit Logs", "The audit log on your dashboard records every significant action in the system including logins, approvals, rejections, and account changes.")
    ))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(role: String?, onNavigateBack: () -> Unit) {
    val guide = HELP_CONTENT[role]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Support", color = ClearanceTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("\u2190", color = ClearanceTextPrimary, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ClearanceSurface, titleContentColor = ClearanceTextPrimary)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().background(ClearanceDark).padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "\uD83D\uDCD6 Help & Support", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text(text = "Step by step guide for using the School Clearance System", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (guide == null) {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ClearanceSurface), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Not Logged In", color = ClearanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Please log in to see your personalised help guide.", color = ClearanceTextMuted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            } else {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = ClearanceSurface), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "${guide.icon} ${guide.title}", color = ClearanceTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                        guide.steps.forEachIndexed { index, step ->
                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                Box(
                                    modifier = Modifier.size(28.dp).clip(CircleShape).background(ClearancePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = (index + 1).toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = step.title, color = ClearanceTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = step.body, color = ClearanceGray700, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}