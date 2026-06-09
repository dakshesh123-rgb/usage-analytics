package com.outsiders.usage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.outsiders.usage.data.db.dao.DailyUsageDao
import com.outsiders.usage.domain.analyzer.DayOverDayAnalyzer
import com.outsiders.usage.domain.analyzer.HourlyAnalyzer
import com.outsiders.usage.domain.analyzer.LateNightAnalyzer
import com.outsiders.usage.domain.analyzer.PickupAnalyzer
import com.outsiders.usage.domain.analyzer.SequenceAnalyzer
import com.outsiders.usage.domain.analyzer.TopAppsAnalyzer
import com.outsiders.usage.viewmodel.AppsViewModel
import com.outsiders.usage.viewmodel.DashboardViewModel
import com.outsiders.usage.viewmodel.InsightsViewModel

class DashboardViewModelFactory(
    private val topAppsAnalyzer: TopAppsAnalyzer,
    private val hourlyAnalyzer: HourlyAnalyzer,
    private val dailyUsageDao: DailyUsageDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(topAppsAnalyzer, hourlyAnalyzer, dailyUsageDao) as T
    }
}

class AppsViewModelFactory(
    private val dailyUsageDao: DailyUsageDao
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppsViewModel(dailyUsageDao) as T
    }
}

class InsightsViewModelFactory(
    private val sequenceAnalyzer: SequenceAnalyzer,
    private val dayOverDayAnalyzer: DayOverDayAnalyzer,
    private val pickupAnalyzer: PickupAnalyzer,
    private val lateNightAnalyzer: LateNightAnalyzer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InsightsViewModel(
            sequenceAnalyzer, dayOverDayAnalyzer, pickupAnalyzer, lateNightAnalyzer
        ) as T
    }
}
