package com.outsiders.usage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.outsiders.usage.data.worker.UsageStatsWorker
import com.outsiders.usage.ui.navigation.AppNavigation
import com.outsiders.usage.ui.theme.UsageAnalyticsTheme
import com.outsiders.usage.viewmodel.AppsViewModel
import com.outsiders.usage.viewmodel.DashboardViewModel
import com.outsiders.usage.viewmodel.InsightsViewModel

class MainActivity : ComponentActivity() {

    // ViewModels
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var appsViewModel: AppsViewModel
    private lateinit var insightsViewModel: InsightsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initViewModels()
        UsageStatsWorker.enqueuePeriodic(this)

        setContent {
            UsageAnalyticsTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        dashboardViewModel = dashboardViewModel,
                        appsViewModel = appsViewModel,
                        insightsViewModel = insightsViewModel
                    )
                }
            }
        }
    }

    private fun initViewModels() {
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        appsViewModel = ViewModelProvider(this).get(AppsViewModel::class.java)
        insightsViewModel = ViewModelProvider(this).get(InsightsViewModel::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        UsageStatsWorker.cancel(this)
    }
}
