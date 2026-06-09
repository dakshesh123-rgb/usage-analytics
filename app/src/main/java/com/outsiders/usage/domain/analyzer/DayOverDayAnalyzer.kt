package com.outsiders.usage.domain.analyzer

import com.outsiders.usage.data.db.dao.DailyUsageDao
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DayOverDayResult(
    val todayMinutes: Int,
    val yesterdayMinutes: Int,
    val percentChange: Double,
    val isUp: Boolean
)

class DayOverDayAnalyzer(private val dailyUsageDao: DailyUsageDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend fun getDayOverDay(): DayOverDayResult {
        val today = dateFormat.format(Calendar.getInstance().time)

        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterday = dateFormat.format(yesterdayCal.time)

        val todayTotal = dailyUsageDao.getTotalMinutesForDate(today).first() ?: 0
        val yesterdayTotal = dailyUsageDao.getTotalMinutesForDate(yesterday).first() ?: 0

        val percentChange = if (yesterdayTotal > 0) {
            ((todayTotal - yesterdayTotal).toDouble() / yesterdayTotal) * 100.0
        } else {
            0.0
        }

        return DayOverDayResult(
            todayMinutes = todayTotal,
            yesterdayMinutes = yesterdayTotal,
            percentChange = kotlin.math.abs(percentChange),
            isUp = todayTotal >= yesterdayTotal
        )
    }
}
