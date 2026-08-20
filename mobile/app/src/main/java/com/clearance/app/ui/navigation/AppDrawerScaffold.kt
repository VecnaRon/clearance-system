package com.clearance.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clearance.app.ui.theme.ClearanceDanger
import com.clearance.app.ui.theme.ClearancePrimary
import com.clearance.app.ui.theme.ClearanceScreenBackground
import com.clearance.app.ui.theme.ClearanceSurface
import com.clearance.app.ui.theme.ClearanceTextPrimary
import kotlinx.coroutines.launch

data class DrawerItem(
    val label: String,
    val onClick: () -> Unit,
    val isSelected: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerScaffold(
    title: String,
    drawerItems: List<DrawerItem>,
    onLogout: () -> Unit,
    topBarActions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = ClearanceSurface) {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Text(
                        text = "ClearanceSystem",
                        color = ClearancePrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    drawerItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item.label, color = ClearanceTextPrimary) },
                            selected = item.isSelected,
                            onClick = {
                                scope.launch { drawerState.close() }
                                item.onClick()
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = ClearancePrimary.copy(alpha = 0.12f),
                                unselectedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    NavigationDrawerItem(
                        label = { Text("Logout", color = ClearanceDanger, fontWeight = FontWeight.SemiBold) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onLogout()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            containerColor = ClearanceScreenBackground,
            topBar = {
                TopAppBar(
                    title = { Text(title, color = ClearanceTextPrimary, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Text("\u2630", color = ClearanceTextPrimary, fontSize = 20.sp)
                        }
                    },
                    actions = topBarActions,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ClearanceSurface,
                        titleContentColor = ClearanceTextPrimary
                    )
                )
            },
            content = content
        )
    }
}