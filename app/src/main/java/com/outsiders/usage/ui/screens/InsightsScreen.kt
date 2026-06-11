package com.outsiders.usage.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.outsiders.usage.ui.components.InsightCard
import com.outsiders.usage.ui.theme.Cyan
import com.outsiders.usage.viewmodel.InsightsViewModel

@Composable
fun InsightsScreen(viewModel: InsightsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Insights",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Sequences card
        state.topSequences.forEach { seq ->
            InsightCard(
                title = "${seq.fromApp} \u2192 ${seq.toApp}",
                subtitle = "${seq.count} transitions, typically at ${seq.typicalTime}",
                icon = Icons.Default.Timeline,
                accentColor = Cyan,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Pickups card
        state.pickups?.let { pk ->
            InsightCard(
                title = "Phone Pickups",
                subtitle = "${pk.totalPickups} pickups today, peak at hour ${pk.peakHour}",
                icon = Icons.Default.PhoneAndroid,
                accentColor = Cyan
            )
        }

        // Late night card
        state.lateNight?.let { ln ->
            InsightCard(
                title = "Late Night Usage",
                subtitle = "${ln.totalMinutes} min across ${ln.sessionCount} apps, latest at ${ln.latestSessionEnd}",
                icon = Icons.Default.DarkMode,
                accentColor = Cyan,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
