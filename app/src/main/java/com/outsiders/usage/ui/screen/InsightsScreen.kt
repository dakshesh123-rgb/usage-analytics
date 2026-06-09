package com.outsiders.usage.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.outsiders.usage.ui.components.InsightCard
import com.outsiders.usage.ui.components.InsightType
import com.outsiders.usage.ui.viewmodel.InsightsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { padding ->
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Day-over-day comparison
                item {
                    Text(
                        text = "Day Over Day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                state.dayOverDay?.let { dodo ->
                    item {
                        val minDelta = if (dodo.minutesDelta >= 0) "+${dodo.minutesDelta}" else "${dodo.minutesDelta}"
                        val type = when {
                            dodo.minutesDelta < 0 -> InsightType.POSITIVE
                            dodo.minutesDelta > 10 -> InsightType.NEGATIVE
                            else -> InsightType.NEUTRAL
                        }
                        InsightCard(
                            title = "Screen Time: $minDelta min",
                            description = "Today: ${dodo.today.totalMinutes}m vs Yesterday: ${dodo.yesterday.totalMinutes}m",
                            type = type
                        )
                    }
                }

                // Pickup pattern
                item {
                    Text(
                        text = "Pickup Pattern",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                state.pickupSummary?.let { pickup ->
                    item {
                        InsightCard(
                            title = "${pickup.totalPickups} pickups today",
                            description = "First pickup at hour ${pickup.firstPickupHour}",
                            type = InsightType.NEUTRAL
                        )
                    }
                }

                // Late night usage
                item {
                    Text(
                        text = "Late Night Usage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                state.lateNight?.let { late ->
                    item {
                        val type = if (late.totalEvents > 10) InsightType.NEGATIVE else InsightType.NEUTRAL
                        InsightCard(
                            title = "${late.totalEvents} late-night events",
                            description = "${late.uniqueApps} apps used after midnight",
                            type = type
                        )
                    }
                }

                // App sequences / transitions
                item {
                    Text(
                        text = "App Sequences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (state.sequences.isEmpty()) {
                    item {
                        InsightCard(
                            title = "No sequences yet",
                            description = "Open multiple apps in quick succession to see patterns",
                            type = InsightType.NEUTRAL
                        )
                    }
                } else {
                    items(state.sequences) { seq ->
                        InsightCard(
                            title = "${seq.fromApp} → ${seq.toApp}",
                            description = "${seq.count} times · typically at ${seq.typicalTime}",
                            type = InsightType.NEUTRAL
                        )
                    }
                }
            }
        }
    }
}
