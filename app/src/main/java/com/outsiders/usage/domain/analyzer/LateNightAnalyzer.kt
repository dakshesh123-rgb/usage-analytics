package com.outsiders.usage.domain.analyzer

import com.outsiders.usage.data.db.dao.UsageEventDao
import kotlinx.coroutines.flow.first
import java.util.Calendar

data class LateNightResult(
    val totalMinutes: Int,
    val sessionCount: Int,
    val latestSessionEnd: String  // formatted time e.g. "2:30 AM"
)

class LateNightAnalyzer(private val usageEventDao: UsageEventDao) {

    suspend fun analyze(): LateNightResult {
        val now = System.currentTimeMillis()
        val lastNight = now - 86400000  // 24 hours back

        val events = usageEventDao.getEventsSince(lastNight).first()
        val nightEvents = events.filter {
            val hour = getHour(it.timestamp)
            hour in 0..5 || hour >= 22
        }

        val cal = Calendar.getInstance()
        var latestTimestamp = 0L

        for (event in nightEvents) {
            if (event.timestamp > latestTimestamp) {
                latestTimestamp = event.timestamp
            }
        }

        val totalMinutes = nightEvents.size * 5  // estimate: each event ~5 min
        val sessionCount = nightEvents.distinctBy { it.packageName }.count()
        val latestTime = if (latestTimestamp > 0) {
            cal.timeInMillis = latestTimestamp
            String.format(
                "%d:%02d %s",
                cal.get(Calendar.HOUR),
                cal.get(Calendar.MINUTE),
                if (cal.get(Calendar.AM_PM) == Calendar.AM) "AM" else "PM"
            )
        } else {
            "N/A"
        }

        return LateNightResult(
            totalMinutes = totalMinutes,
            sessionCount = sessionCount,
            latestSessionEnd = latestTime
        )
    }

    private fun getHour(timestamp: Long): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        return cal.get(Calendar.HOUR_OF_DAY)
    }
}
