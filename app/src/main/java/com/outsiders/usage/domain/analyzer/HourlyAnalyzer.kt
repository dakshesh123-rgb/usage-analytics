package com.outsiders.usage.domain.analyzer

import com.outsiders.usage.data.db.dao.UsageEventDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar

data class HourlyBucket(
    val hour: Int,  // 0-23
    val eventCount: Int,
    val appsUsed: Int
)

data class HourlyResult(
    val buckets: List<HourlyBucket>,
    val peakHour: Int,
    val peakCount: Int
)

class HourlyAnalyzer(private val usageEventDao: UsageEventDao) {

    fun getHourlyBreakdown(): Flow<List<HourlyBucket>> {
        val todayStart = getTodayStart()
        val todayEnd = todayStart + 86400000

        return usageEventDao.getEventsForDay(todayStart, todayEnd).map { events ->
            val buckets = Array(24) { HourlyBucket(it, 0, 0) }
            val appsInHour = Array<MutableSet<String>>(24) { mutableSetOf() }

            for (event in events) {
                if (event.eventType == "OPEN") {
                    val hour = getHour(event.timestamp)
                    if (hour in 0..23) {
                        buckets[hour] = buckets[hour].copy(eventCount = buckets[hour].eventCount + 1)
                        appsInHour[hour].add(event.packageName)
                    }
                }
            }

            // Fill in apps used per hour
            for (h in 0..23) {
                buckets[h] = buckets[h].copy(appsUsed = appsInHour[h].size)
            }

            buckets.toList()
        }
    }

    suspend fun getHourlySnapshot(): HourlyResult {
        val buckets = getHourlyBreakdown().first()
        val peak = buckets.maxByOrNull { it.eventCount }
        return HourlyResult(
            buckets = buckets,
            peakHour = peak?.hour ?: 0,
            peakCount = peak?.eventCount ?: 0
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

    private fun getHour(timestamp: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal.get(Calendar.HOUR_OF_DAY)
    }
}
