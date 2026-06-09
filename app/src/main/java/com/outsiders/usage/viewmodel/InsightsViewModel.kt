package com.outsiders.usage.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.outsiders.usage.data.db.AppDatabase
import com.outsiders.usage.domain.analyzer.SequenceAnalyzer
import com.outsiders.usage.domain.analyzer.PickupAnalyzer
import com.outsiders.usage.domain.analyzer.LateNightAnalyzer
import com.outsiders.usage.domain.analyzer.AppSequence
import com.outsiders.usage.domain.analyzer.PickupResult
import com.outsiders.usage.domain.analyzer.LateNightResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InsightsState(
    val topSequences: List<AppSequence> = emptyList(),
    val pickups: PickupResult? = null,
    val lateNight: LateNightResult? = null,
    val isLoading: Boolean = true
)

class InsightsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val sequenceAnalyzer = SequenceAnalyzer(db.usageEventDao())
    private val pickupAnalyzer = PickupAnalyzer(db.usageEventDao())
    private val lateNightAnalyzer = LateNightAnalyzer(db.usageEventDao())

    private val _state = MutableStateFlow(InsightsState())
    val state: StateFlow<InsightsState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val sequences = sequenceAnalyzer.getTopSequences()
                val pickups = pickupAnalyzer.analyze()
                val lateNight = lateNightAnalyzer.analyze()
                _state.value = InsightsState(
                    topSequences = sequences,
                    pickups = pickups,
                    lateNight = lateNight,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}
