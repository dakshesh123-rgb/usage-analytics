package com.outsiders.usage.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.outsiders.usage.data.db.AppDatabase
import com.outsiders.usage.domain.analyzer.DayOverDayAnalyzer
import com.outsiders.usage.domain.analyzer.HourlyAnalyzer
import com.outsiders.usage.domain.analyzer.TopAppsAnalyzer
import com.outsiders.usage.domain.analyzer.AppUsageSummary
import com.outsiders.usage.domain.analyzer.HourlyBucket
import com.outsiders.usage.domain.analyzer.DayOverDayResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class DashboardState(
    val topApps: List<AppUsageSummary> = emptyList(),
    val hourlyBuckets: List<HourlyBucket> = emptyList(),
    val dayOverDay: DayOverDayResult? = null,
    val isLoading: Boolean = true
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val topAppsAnalyzer = TopAppsAnalyzer(db.dailyUsageDao())
    private val hourlyAnalyzer = HourlyAnalyzer(db.usageEventDao())
    private val dayOverDayAnalyzer = DayOverDayAnalyzer(db.dailyUsageDao())

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val topApps = topAppsAnalyzer.getTopAppsSnapshot(limit = 5)
                val hourly = hourlyAnalyzer.getHourlyBreakdown().first()
                val dod = dayOverDayAnalyzer.getDayOverDay()
                _state.value = DashboardState(
                    topApps = topApps.apps,
                    hourlyBuckets = hourly,
                    dayOverDay = dod,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}
