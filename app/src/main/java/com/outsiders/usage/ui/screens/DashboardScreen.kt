package com.outsiders.usage.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.outsiders.usage.ui.components.AppRow
import com.outsiders.usage.ui.components.StatCard
import com.outsiders.usage.ui.theme.Cyan
import com.outsiders.usage.ui.theme.ErrorRed
import com.outsiders.usage.ui.theme.Gold
import com.outsiders.usage.ui.theme.SuccessGreen
import com.outsiders.usage.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (state.topApps.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No usage data yet",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grant Usage Access permission in Settings\nto start tracking your app usage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
            text = "Today",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Stat cards row
        Row(modifier = Modifier.fillMaxWidth()) {
            StatCard(
                title = "Total Time",
                value = "${state.dayOverDay?.todayMinutes ?: 0}m",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            StatCard(
                title = "Apps Used",
                value = "${state.topApps.size}",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Day-over-day change
        state.dayOverDay?.let { dod ->
            val arrow = if (dod.isUp) "\u2191" else "\u2193"
            val changeColor = if (dod.isUp) SuccessGreen else ErrorRed
            Text(
                text = "${dod.todayMinutes}m today vs ${dod.yesterdayMinutes}m yesterday",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$arrow ${String.format("%.1f", dod.percentChange)}% vs yesterday",
                style = MaterialTheme.typography.labelLarge,
                color = changeColor
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Top apps section
        Text(
            text = "Top Apps",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        state.topApps.forEach { app ->
            AppRow(
                packageName = app.packageName,
                appName = app.appName,
                totalMinutes = app.totalMinutes,
                openCount = app.openCount,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Hourly sparkline
        Text(
            text = "Hourly Activity",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (state.hourlyBuckets.isNotEmpty()) {
            val maxCount = state.hourlyBuckets.maxOfOrNull { it.eventCount }?.coerceAtLeast(1) ?: 1
            Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                val barWidth = size.width / 24f
                state.hourlyBuckets.forEachIndexed { hour, bucket ->
                    val barHeight = (bucket.eventCount.toFloat() / maxCount) * size.height
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Cyan, Gold),
                            startY = size.height,
                            endY = 0f
                        ),
                        topLeft = Offset(hour * barWidth + 2f, size.height - barHeight),
                        size = Size(barWidth - 4f, barHeight)
                    )
                }
            }
        }
    }
}
