package com.outsiders.usage.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.outsiders.usage.data.db.AppDatabase
import com.outsiders.usage.domain.analyzer.HourlyAnalyzer
import com.outsiders.usage.domain.analyzer.HourlyBreakdown
import com.outsiders.usage.domain.analyzer.TopAppInfo
import com.outsiders.usage.domain.analyzer.TopAppsAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardState(
    val totalScreenTime: Int = 0,
    val appCount: Int = 0,
    val topApps: List<TopAppInfo> = emptyList(),
    val hourlyBreakdown: List<HourlyBreakdown> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val topAppsAnalyzer = TopAppsAnalyzer(db.dailyUsageDao())
    private val hourlyAnalyzer = HourlyAnalyzer(db.usageEventDao())

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val totalScreenTime = topAppsAnalyzer.getTotalScreenTime(today)
                val appCount = topAppsAnalyzer.getAppCount(today)
                val topApps = topAppsAnalyzer.getTopApps(today)
                val hourlyBreakdown = hourlyAnalyzer.getHourlyBreakdown()

                _state.value = DashboardState(
                    totalScreenTime = totalScreenTime,
                    appCount = appCount,
                    topApps = topApps,
                    hourlyBreakdown = hourlyBreakdown,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard"
                )
            }
        }
    }
}
