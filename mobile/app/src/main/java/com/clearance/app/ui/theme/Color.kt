package com.clearance.app.ui.theme

import androidx.compose.ui.graphics.Color

// These colors mirror client/src/theme/theme.js from the existing
// web app, so the Android app keeps the same brand identity.

val ClearancePrimary = Color(0xFF3B82F6)   // theme.colors.primary
val ClearanceAccent = Color(0xFF8B5CF6)    // theme.colors.accent
val ClearanceSuccess = Color(0xFF10B981)   // theme.colors.success
val ClearanceDanger = Color(0xFFEF4444)    // theme.colors.danger
val ClearanceWarning = Color(0xFFF59E0B)   // theme.colors.warning
val ClearanceInfo = Color(0xFF06B6D4)      // theme.colors.info

val ClearanceDarkBg = Color(0xFF1E293B)    // theme.colors.darkBg
val ClearanceDark = Color(0xFF0F172A)      // theme.colors.dark
val ClearanceText = Color(0xFF1E293B)      // theme.colors.text
val ClearanceLight = Color(0xFFF8FAFC)     // theme.colors.light
val ClearanceBorder = Color(0xFFE2E8F0)    // theme.colors.border

val ClearanceGray100 = Color(0xFFF1F5F9)
val ClearanceGray300 = Color(0xFFCBD5E1)
val ClearanceGray600 = Color(0xFF475569)
val ClearanceGray700 = Color(0xFF334155)

// Explicit, guaranteed-contrast text colors — always dark, because
// this app is light-only by design (see Theme.kt). Used directly
// instead of ambiguous MaterialTheme.colorScheme roles.
val ClearanceTextPrimary = Color(0xFF0F172A)   // headings — near-black navy
val ClearanceTextSecondary = Color(0xFF334155) // body text — dark slate
val ClearanceTextMuted = Color(0xFF64748B)     // labels/meta text — medium slate, WCAG AA on white

// UI STABILIZATION PASS additions — explicit surface/component colors
// so no Scaffold/Dialog/Field/Snackbar ever falls back to a theme
// role that could resolve to a dark value.
val ClearanceScreenBackground = ClearanceLight   // app background, all screens
val ClearanceSurface = Color(0xFFFFFFFF)         // cards/surfaces
val ClearanceInputBackground = Color(0xFFFFFFFF) // text field fill
val ClearanceInputLabel = ClearanceTextMuted      // text field label
val ClearanceDialogBackground = Color(0xFFFFFFFF) // AlertDialog surface
val ClearanceSnackbarBackground = ClearanceDark   // dark pill, Material convention
val ClearanceSnackbarText = Color(0xFFFFFFFF)
val ClearanceButtonText = Color(0xFFFFFFFF)       // text/icon color on filled primary buttons