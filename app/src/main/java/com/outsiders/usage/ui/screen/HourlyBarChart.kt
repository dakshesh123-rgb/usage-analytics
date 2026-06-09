package com.outsiders.usage.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outsiders.usage.domain.analyzer.HourlyBreakdown
import com.outsiders.usage.ui.theme.AccentCyan

@Composable
fun HourlyBarChart(
    hourlyData: List<HourlyBreakdown>,
    modifier: Modifier = Modifier
) {
    val maxCount = hourlyData.maxOfOrNull { it.openCount } ?: 1
    val barColor = AccentCyan
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
    val textMeasurer = rememberTextMeasurer()

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val barWidth = size.width / 24f
            val chartHeight = size.height - 20f

            // Grid lines
            for (i in 0..4) {
                val y = chartHeight * (1f - i / 4f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }

            // Bars
            for (breakdown in hourlyData) {
                val barHeight = if (maxCount > 0) {
                    (breakdown.openCount.toFloat() / maxCount) * chartHeight
                } else 0f
                val x = breakdown.hour * barWidth + barWidth * 0.15f
                val w = barWidth * 0.7f

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, chartHeight - barHeight),
                    size = Size(w, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )

                // Hour label
                if (breakdown.hour % 3 == 0) {
                    val label = "${breakdown.hour}"
                    val textResult = textMeasurer.measure(
                        text = label,
                        style = TextStyle(
                            color = gridColor,
                            fontSize = 9.sp
                        )
                    )
                    drawText(
                        textLayoutResult = textResult,
                        topLeft = Offset(
                            x + (w - textResult.size.width) / 2f,
                            chartHeight + 4f
                        )
                    )
                }
            }
        }

        Text(
            text = "Open count per hour",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
