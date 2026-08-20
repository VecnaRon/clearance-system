package com.clearance.app.ui.screens

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clearance.app.data.local.SessionManager
import com.clearance.app.ui.theme.ClearanceAccent
import com.clearance.app.ui.theme.ClearanceGray100
import com.clearance.app.ui.theme.ClearancePrimary
import com.clearance.app.ui.theme.ClearanceScreenBackground
import com.clearance.app.ui.theme.ClearanceSurface
import com.clearance.app.ui.theme.ClearanceTextMuted
import com.clearance.app.ui.theme.ClearanceTextPrimary
import com.clearance.app.ui.theme.ClearanceTextSecondary

private data class Faq(val q: String, val a: String)

/** Content copied verbatim from client/src/pages/Home.js. */
private val FAQS = listOf(
    Faq("Why can I not log in after registering?", "Your account needs to be activated by the administrator before you can log in. Please wait for activation or contact your school office."),
    Faq("Why is my clearance rejected?", "A department has found outstanding items against your account such as unreturned books or unpaid fees. Visit the relevant department to resolve the issue then ask the staff member to mark it as resolved."),
    Faq("How long does the clearance process take?", "This depends on how quickly each department reviews your request and how fast you resolve any outstanding items. Once all departments clear you the admin gives final approval."),
    Faq("Can I resubmit my clearance request?", "No. You can only submit one clearance request. If there are issues the relevant department will update your status directly."),
    Faq("Where do I download my clearance certificate?", "Once your clearance is fully approved go to the Downloads page from your navigation bar to download your certificate as a PDF.")
)

/**
 * Reproduces client/src/pages/Home.js — the web app's public "/" route.
 * Auto-routes an already-logged-in session (SessionManager.isLoggedIn())
 * straight to its role dashboard on mount, then renders the normal
 * public landing content for everyone else.
 */
@Composable
fun HomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onAlreadyLoggedIn: (role: String) -> Unit
) {
    LaunchedEffect(Unit) {
        val user = SessionManager.getUser()
        if (SessionManager.isLoggedIn() && user != null) {
            onAlreadyLoggedIn(user.role)
        }
    }

    Scaffold(containerColor = ClearanceScreenBackground) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            HeroSection(onNavigateToRegister, onNavigateToLogin)
            Spacer(modifier = Modifier.height(20.dp))
            StatsSection()
            Spacer(modifier = Modifier.height(20.dp))
            FeaturesSection(onNavigateToRegister)
            Spacer(modifier = Modifier.height(20.dp))
            AboutSection()
            Spacer(modifier = Modifier.height(20.dp))
            FaqSection()
            Spacer(modifier = Modifier.height(20.dp))
            ContactSection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeroSection(onNavigateToRegister: () -> Unit, onNavigateToLogin: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(colors = listOf(ClearancePrimary, ClearanceAccent), start = Offset(0f, 0f), end = Offset(1000f, 1000f)))
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Digital School Clearance System", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Text(
                text = "Revolutionizing the clearance process for Kenyan high schools with modern technology",
                color = Color.White.copy(alpha = 0.95f), fontSize = 15.sp, textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = "Streamline operations, reduce paperwork, and track progress in real-time",
                color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
            )
            Button(
                onClick = onNavigateToRegister,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ClearancePrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Get Started Today", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onNavigateToLogin,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            ) {
                Text("Sign In", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun StatsSection() {
    val stats = listOf("98%" to "Success Rate", "75%" to "Time Saved", "24/7" to "Available", "100+" to "Schools Served")
    Row(modifier = Modifier.fillMaxWidth()) {
        stats.forEachIndexed { index, (number, label) ->
            Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = ClearanceSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = number, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = ClearancePrimary)
                    Text(text = label, fontSize = 9.sp, color = ClearanceTextMuted, textAlign = TextAlign.Center)
                }
            }
            if (index != stats.lastIndex) Spacer(modifier = Modifier.width(6.dp))
        }
    }
}

@Composable
private fun FeaturesSection(onNavigateToRegister: () -> Unit) {
    Column {
        FeatureCard("\uD83C\uDF93", "For Students",
            "Submit clearance requests online and track your progress through each department. No more running around campus or waiting in long queues. Get real-time updates and notifications on your clearance status.",
            ctaLabel = "Get Started \u2192", onClick = onNavigateToRegister)
        Spacer(modifier = Modifier.height(10.dp))
        FeatureCard("\uD83C\uDFE2", "For Departments",
            "Efficiently review student clearances with our intuitive dashboard. Manage outstanding dues, add detailed remarks, and approve or reject requests with comprehensive tracking and reporting capabilities.")
        Spacer(modifier = Modifier.height(10.dp))
        FeatureCard("\u2699\uFE0F", "For Administrators",
            "Complete oversight of the entire clearance ecosystem. Manage departments and users, generate comprehensive reports, and maintain system integrity with advanced administrative tools and analytics.")
    }
}

@Composable
private fun FeatureCard(icon: String, title: String, body: String, ctaLabel: String? = null, onClick: (() -> Unit)? = null) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ClearanceSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().let { if (onClick != null) it.clickable(onClick = onClick) else it }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(text = icon, fontSize = 30.sp)
            Text(text = title, color = ClearanceTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
            Text(text = body, color = ClearanceTextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
            if (ctaLabel != null) {
                Text(text = ctaLabel, color = ClearancePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
            }
        }
    }
}

@Composable
private fun AboutSection() {
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = ClearanceSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "\uD83D\uDCCB About This System", color = ClearanceTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
            Text(
                text = "The Digital School Clearance System is a modern web-based platform designed to streamline the clearance process for Kenyan high schools. It replaces manual paper-based workflows with a fast, transparent, and accountable digital process \u2014 connecting students, department staff, heads of department, and school administrators on a single platform.",
                color = ClearanceTextSecondary, fontSize = 13.sp, lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun FaqSection() {
    var openIndex by remember { mutableStateOf<Int?>(null) }

    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = ClearanceSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "\u2753 Frequently Asked Questions", color = ClearanceTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            FAQS.forEachIndexed { index, faq ->
                val isOpen = openIndex == index
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ClearanceGray100)
                        .clickable { openIndex = if (isOpen) null else index }
                        .animateContentSize()
                        .padding(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = faq.q, color = ClearanceTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(text = if (isOpen) "\u25B2" else "\u25BC", color = ClearanceTextMuted, fontSize = 11.sp)
                    }
                    if (isOpen) {
                        Text(text = faq.a, color = ClearanceTextSecondary, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactSection() {
    val items = listOf(
        Triple("\uD83C\uDFEB", "School", "Mambotela High School"),
        Triple("\uD83D\uDCDE", "Phone", "+254 700 000 000"),
        Triple("\uD83D\uDCE7", "Email", "admin@clearance.com"),
        Triple("\uD83D\uDCCD", "Location", "Nairobi, Kenya")
    )
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = ClearanceSurface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "\uD83D\uDCEC Contact Us", color = ClearanceTextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            items.forEach { (icon, label, value) ->
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = icon, fontSize = 18.sp, modifier = Modifier.padding(end = 10.dp))
                    Column {
                        Text(text = label.uppercase(), fontSize = 10.sp, color = ClearanceTextMuted, fontWeight = FontWeight.SemiBold)
                        Text(text = value, fontSize = 13.sp, color = ClearanceTextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}