package com.outsiders.usage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outsiders.usage.data.db.dao.DailyUsageDao
import com.outsiders.usage.data.db.entity.DailyUsage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppsUiState(
    val apps: List<DailyUsage> = emptyList(),
    val totalMinutes: Int = 0,
    val isLoading: Boolean = false,
    val searchQuery: String = ""
)

class AppsViewModel(
    private val dailyUsageDao: DailyUsageDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    init {
        loadApps()
    }

    fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val date = dateFormat.format(Date())
            val usages = dailyUsageDao.getUsageForDate(date).first()
            val total = usages.sumOf { it.totalMinutes }

            _uiState.update {
                it.copy(
                    apps = usages,
                    totalMinutes = total,
                    isLoading = false
                )
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getFilteredApps(): List<DailyUsage> {
        val state = _uiState.value
        return if (state.searchQuery.isBlank()) {
            state.apps
        } else {
            state.apps.filter {
                it.appName.contains(state.searchQuery, ignoreCase = true) ||
                    it.packageName.contains(state.searchQuery, ignoreCase = true)
            }
        }
    }
}
