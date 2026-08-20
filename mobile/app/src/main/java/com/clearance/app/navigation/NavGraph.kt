package com.clearance.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.clearance.app.data.local.SessionManager
import com.clearance.app.ui.screens.AdminAuditScreen
import com.clearance.app.ui.screens.AdminDashboardScreen
import com.clearance.app.ui.screens.AdminDepartmentsScreen
import com.clearance.app.ui.screens.AdminReportsScreen
import com.clearance.app.ui.screens.AdminUsersScreen
import com.clearance.app.ui.screens.ClearanceFormScreen
import com.clearance.app.ui.screens.DepartmentDashboardScreen
import com.clearance.app.ui.screens.DepartmentStudentReviewScreen
import com.clearance.app.ui.screens.HelpScreen
import com.clearance.app.ui.screens.HomeScreen
import com.clearance.app.ui.screens.LoginScreen
import com.clearance.app.ui.screens.RegisterScreen
import com.clearance.app.ui.screens.RolePlaceholderScreen
import com.clearance.app.ui.screens.Stage2HomeScreen
import com.clearance.app.ui.screens.StaffDashboardScreen
import com.clearance.app.ui.screens.StudentClearanceStatusScreen
import com.clearance.app.ui.screens.StudentStartScreen

object Routes {
    const val STAGE2_HOME = "stage2_home"
    const val HOME = "home"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val STUDENT_FORM = "student_form"
    const val STUDENT_STATUS = "student_status"
    const val ADMIN_DEPARTMENTS = "admin_departments"
    const val ADMIN_USERS = "admin_users"
    const val ADMIN_REPORTS = "admin_reports"
    const val ADMIN_AUDIT = "admin_audit"
    const val HELP = "help"
    private const val ROLE_HOME_PATTERN = "role_home/{role}"
    const val ROLE_HOME = ROLE_HOME_PATTERN
    private const val DEPARTMENT_REVIEW_PATTERN = "department_review/{stepId}"
    const val DEPARTMENT_REVIEW = DEPARTMENT_REVIEW_PATTERN

    fun roleHome(role: String) = "role_home/$role"
    fun departmentReview(stepId: Int) = "department_review/$stepId"
}

data class AdminNavActions(
    val onDashboard: () -> Unit,
    val onDepartments: () -> Unit,
    val onUsers: () -> Unit,
    val onReports: () -> Unit,
    val onAudit: () -> Unit,
    val onHelp: () -> Unit,
    val onLogout: () -> Unit
)

data class StudentNavActions(
    val onStart: () -> Unit,
    val onStatus: () -> Unit,
    val onDownloads: () -> Unit,
    val onHelp: () -> Unit,
    val onLogout: () -> Unit
)

data class DepartmentNavActions(
    val onDepartment: () -> Unit,
    val onHelp: () -> Unit,
    val onLogout: () -> Unit
)

data class StaffNavActions(
    val onDashboard: () -> Unit,
    val onHelp: () -> Unit,
    val onLogout: () -> Unit
)

private fun buildAdminNavActions(navController: NavHostController): AdminNavActions {
    fun go(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.roleHome("admin")) { inclusive = false }
            launchSingleTop = true
        }
    }
    return AdminNavActions(
        onDashboard = { go(Routes.roleHome("admin")) },
        onDepartments = { go(Routes.ADMIN_DEPARTMENTS) },
        onUsers = { go(Routes.ADMIN_USERS) },
        onReports = { go(Routes.ADMIN_REPORTS) },
        onAudit = { go(Routes.ADMIN_AUDIT) },
        onHelp = { go(Routes.HELP) },
        onLogout = {
            SessionManager.clearAll()
            navController.navigate(Routes.LOGIN) { popUpTo(0) }
        }
    )
}

private fun buildStudentNavActions(navController: NavHostController): StudentNavActions {
    fun go(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.roleHome("student")) { inclusive = false }
            launchSingleTop = true
        }
    }
    return StudentNavActions(
        onStart = { go(Routes.roleHome("student")) },
        onStatus = { go(Routes.STUDENT_STATUS) },
        onDownloads = { go(Routes.STUDENT_STATUS) },
        onHelp = { go(Routes.HELP) },
        onLogout = {
            SessionManager.clearAll()
            navController.navigate(Routes.LOGIN) { popUpTo(0) }
        }
    )
}

private fun buildDepartmentNavActions(navController: NavHostController): DepartmentNavActions {
    fun go(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.roleHome("hod")) { inclusive = false }
            launchSingleTop = true
        }
    }
    return DepartmentNavActions(
        onDepartment = { go(Routes.roleHome("hod")) },
        onHelp = { go(Routes.HELP) },
        onLogout = {
            SessionManager.clearAll()
            navController.navigate(Routes.LOGIN) { popUpTo(0) }
        }
    )
}

private fun buildStaffNavActions(navController: NavHostController): StaffNavActions {
    fun go(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.roleHome("staff")) { inclusive = false }
            launchSingleTop = true
        }
    }
    return StaffNavActions(
        onDashboard = { go(Routes.roleHome("staff")) },
        onHelp = { go(Routes.HELP) },
        onLogout = {
            SessionManager.clearAll()
            navController.navigate(Routes.LOGIN) { popUpTo(0) }
        }
    )
}

@Composable
fun ClearanceNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.STAGE2_HOME) {
            Stage2HomeScreen()
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                onAlreadyLoggedIn = { role ->
                    navController.navigate(Routes.roleHome(role)) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { role ->
                    navController.navigate(Routes.roleHome(role)) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) { popUpTo(Routes.REGISTER) { inclusive = true } }
                }
            )
        }

        composable(
            route = Routes.ROLE_HOME,
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""

            val onLogout: () -> Unit = {
                SessionManager.clearAll()
                navController.navigate(Routes.LOGIN) { popUpTo(0) }
            }

            when (role) {
                "student" -> StudentStartScreen(
                    studentNav = buildStudentNavActions(navController),
                    onNavigateToForm = { navController.navigate(Routes.STUDENT_FORM) }
                )
                "admin" -> AdminDashboardScreen(adminNav = buildAdminNavActions(navController))
                "hod" -> DepartmentDashboardScreen(
                    departmentNav = buildDepartmentNavActions(navController),
                    onOpenStudent = { stepId -> navController.navigate(Routes.departmentReview(stepId)) }
                )
                "staff" -> StaffDashboardScreen(staffNav = buildStaffNavActions(navController))
                else -> RolePlaceholderScreen(role = role, onLogout = onLogout)
            }
        }

        composable(Routes.STUDENT_FORM) {
            ClearanceFormScreen(
                onNavigateToStatus = {
                    navController.navigate(Routes.STUDENT_STATUS) { popUpTo(Routes.STUDENT_FORM) { inclusive = true } }
                }
            )
        }

        composable(Routes.STUDENT_STATUS) {
            StudentClearanceStatusScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_DEPARTMENTS) {
            AdminDepartmentsScreen(adminNav = buildAdminNavActions(navController))
        }

        composable(Routes.ADMIN_USERS) {
            AdminUsersScreen(adminNav = buildAdminNavActions(navController))
        }

        composable(Routes.ADMIN_REPORTS) {
            AdminReportsScreen(adminNav = buildAdminNavActions(navController))
        }

        composable(Routes.ADMIN_AUDIT) {
            AdminAuditScreen(adminNav = buildAdminNavActions(navController))
        }

        composable(Routes.HELP) {
            HelpScreen(
                role = SessionManager.getUser()?.role,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.DEPARTMENT_REVIEW,
            arguments = listOf(navArgument("stepId") { type = NavType.IntType })
        ) { backStackEntry ->
            val stepId = backStackEntry.arguments?.getInt("stepId") ?: return@composable
            DepartmentStudentReviewScreen(
                stepId = stepId,
                onDecisionComplete = { navController.popBackStack() }
            )
        }
    }
}