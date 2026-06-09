package com.outsiders.usage.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.outsiders.usage.data.db.AppDatabase
import com.outsiders.usage.domain.analyzer.TopAppsAnalyzer
import com.outsiders.usage.domain.analyzer.AppUsageSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AppsState(
    val apps: List<AppUsageSummary> = emptyList(),
    val isLoading: Boolean = true
)

class AppsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val topAppsAnalyzer = TopAppsAnalyzer(db.dailyUsageDao())

    private val _state = MutableStateFlow(AppsState())
    val state: StateFlow<AppsState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val result = topAppsAnalyzer.getTopAppsSnapshot(limit = 50)
                _state.value = AppsState(apps = result.apps, isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}
