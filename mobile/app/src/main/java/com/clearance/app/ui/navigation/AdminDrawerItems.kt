package com.clearance.app.ui.navigation

fun adminDrawerItems(
    adminNav: AdminNavActions,
    selected: String
): List<DrawerItem> {
    return listOf(
        DrawerItem(
            label = "Dashboard",
            onClick = adminNav.onDashboard,
            isSelected = selected == "Dashboard"
        ),

        DrawerItem(
            label = "Departments",
            onClick = adminNav.onDepartments,
            isSelected = selected == "Departments"
        ),

        DrawerItem(
            label = "Users",
            onClick = adminNav.onUsers,
            isSelected = selected == "Users"
        ),

        DrawerItem(
            label = "Reports",
            onClick = adminNav.onReports,
            isSelected = selected == "Reports"
        ),

        DrawerItem(
            label = "Audit Activity",
            onClick = adminNav.onAudit,
            isSelected = selected == "Audit Activity"
        )
    )
}