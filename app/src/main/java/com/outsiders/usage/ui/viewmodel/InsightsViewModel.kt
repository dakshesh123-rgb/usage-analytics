package com.outsiders.usage.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.outsiders.usage.domain.analyzer.DayOverDayAnalyzer
import com.outsiders.usage.domain.analyzer.DayOverDayResult
import com.outsiders.usage.domain.analyzer.LateNightAnalyzer
import com.outsiders.usage.domain.analyzer.LateNightSummary
import com.outsiders.usage.domain.analyzer.PickupAnalyzer
import com.outsiders.usage.domain.analyzer.PickupSummary
import com.outsiders.usage.domain.analyzer.SequenceAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InsightsUiState(
    val sequences: List<com.outsiders.usage.domain.analyzer.AppSequence> = emptyList(),
    val dayOverDay: DayOverDayResult? = null,
    val pickupSummary: PickupSummary? = null,
    val lateNight: LateNightSummary? = null,
    val isLoading: Boolean = false
)

class InsightsViewModel(
    private val sequenceAnalyzer: SequenceAnalyzer,
    private val dayOverDayAnalyzer: DayOverDayAnalyzer,
    private val pickupAnalyzer: PickupAnalyzer,
    private val lateNightAnalyzer: LateNightAnalyzer
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    fun loadInsights() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val sequences = sequenceAnalyzer.getTopSequences()
            val dayOverDay = dayOverDayAnalyzer.getDayOverDay()
            val pickupSummary = pickupAnalyzer.getPickupPattern()
            val lateNight = lateNightAnalyzer.getLateNightUsage()

            _uiState.update {
                it.copy(
                    sequences = sequences,
                    dayOverDay = dayOverDay,
                    pickupSummary = pickupSummary,
                    lateNight = lateNight,
                    isLoading = false
                )
            }
        }
    }
}
