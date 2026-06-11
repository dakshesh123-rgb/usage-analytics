package com.outsiders.usage

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.outsiders.usage.data.collector.UsageStatsCollector
import com.outsiders.usage.data.db.AppDatabase
import com.outsiders.usage.data.worker.UsageStatsWorker
import com.outsiders.usage.ui.navigation.AppNavigation
import com.outsiders.usage.ui.theme.UsageAnalyticsTheme
import com.outsiders.usage.viewmodel.AppsViewModel
import com.outsiders.usage.viewmodel.DashboardViewModel
import com.outsiders.usage.viewmodel.InsightsViewModel
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // ViewModels
    private lateinit var dashboardViewModel: DashboardViewModel
    private lateinit var appsViewModel: AppsViewModel
    private lateinit var insightsViewModel: InsightsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initViewModels()
        UsageStatsWorker.enqueue(this)

        // Immediate data collection so UI isn't empty on first launch
        collectAndRefresh()

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

    override fun onResume() {
        super.onResume()
        // Re-collect when returning from Settings → Usage Access
        collectAndRefresh()
    }

    private fun initViewModels() {
        dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)
        appsViewModel = ViewModelProvider(this).get(AppsViewModel::class.java)
        insightsViewModel = ViewModelProvider(this).get(InsightsViewModel::class.java)
    }

    private fun collectAndRefresh() {
        lifecycleScope.launch {
            try {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val collector = UsageStatsCollector(this@MainActivity)
                val db = AppDatabase.getInstance(this@MainActivity)
                val result = collector.collectFrom(cal.timeInMillis)
                withContext(Dispatchers.IO) {
                    db.dailyUsageDao().insertAll(result.dailyUsages)
                    db.appSessionDao().insertAll(result.sessions)
                    db.usageEventDao().insertAll(result.events)
                }
                dashboardViewModel.refresh()
                appsViewModel.refresh()
                insightsViewModel.refresh()
            } catch (e: Exception) {
                Log.e(TAG, "Collection failed", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        UsageStatsWorker.cancel(this)
    }
}
