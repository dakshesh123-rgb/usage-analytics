package com.outsiders.usage.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.outsiders.usage.ui.screens.AppsScreen
import com.outsiders.usage.ui.screens.DashboardScreen
import com.outsiders.usage.ui.screens.InsightsScreen
import com.outsiders.usage.ui.screens.SettingsScreen
import com.outsiders.usage.viewmodel.AppsViewModel
import com.outsiders.usage.viewmodel.DashboardViewModel
import com.outsiders.usage.viewmodel.InsightsViewModel

data class NavTab(
    val label: String,
    val icon: ImageVector
)

private val tabs = listOf(
    NavTab("Dashboard", Icons.Default.Home),
    NavTab("Apps", Icons.Default.Info),
    NavTab("Insights", Icons.Default.Info),
    NavTab("Settings", Icons.Default.Settings)
)

@Composable
fun AppNavigation(
    dashboardViewModel: DashboardViewModel,
    appsViewModel: AppsViewModel,
    insightsViewModel: InsightsViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        when (selectedTab) {
            0 -> DashboardScreen(
                viewModel = dashboardViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> AppsScreen(
                viewModel = appsViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> InsightsScreen(
                viewModel = insightsViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            3 -> SettingsScreen(
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
