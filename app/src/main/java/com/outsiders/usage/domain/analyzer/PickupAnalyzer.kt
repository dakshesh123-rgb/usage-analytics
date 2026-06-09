package com.outsiders.usage.domain.analyzer

import com.outsiders.usage.data.db.dao.UsageEventDao
import kotlinx.coroutines.flow.first
import java.util.Calendar

data class PickupResult(
    val totalPickups: Int,
    val peakHour: Int,
    val pickupsPerHour: List<Int>  // 24 entries, one per hour
)

class PickupAnalyzer(private val usageEventDao: UsageEventDao) {

    suspend fun analyze(): PickupResult {
        val todayStart = getTodayStart()
        val todayEnd = todayStart + 86400000

        val events = usageEventDao.getEventsForDay(todayStart, todayEnd).first()
        val pickups = events.filter { it.eventType == "OPEN" }

        val hourlyCounts = IntArray(24)
        val cal = Calendar.getInstance()

        for (event in pickups) {
            cal.timeInMillis = event.timestamp
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            if (hour in 0..23) hourlyCounts[hour]++
        }

        return PickupResult(
            totalPickups = pickups.size,
            peakHour = hourlyCounts.indices.maxByOrNull { hourlyCounts[it] } ?: 0,
            pickupsPerHour = hourlyCounts.toList()
        )
    }

    private fun getTodayStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
