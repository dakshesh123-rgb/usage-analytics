package com.outsiders.usage.domain.analyzer

import com.outsiders.usage.data.db.dao.UsageEventDao
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AppSequence(
    val fromApp: String,
    val toApp: String,
    val count: Int,
    val typicalTime: String  // e.g., "9:00 PM"
)

class SequenceAnalyzer(private val usageEventDao: UsageEventDao) {

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

    suspend fun getTopSequences(limit: Int = 5): List<AppSequence> {
        val todayStart = getTodayStart()
        val todayEnd = todayStart + 86400000

        val events = usageEventDao.getEventsForDay(todayStart, todayEnd).first()
        val openEvents = events.filter { it.eventType == "OPEN" }

        if (openEvents.size < 2) return emptyList()

        // Find pairs where App B is opened within 120 seconds of App A
        val pairCounts = mutableMapOf<Pair<String, String>, MutableList<Long>>()
        val windowMs = 120_000L

        for (i in 0 until openEvents.size - 1) {
            val current = openEvents[i]
            val next = openEvents[i + 1]

            if (current.packageName != next.packageName &&
                next.timestamp - current.timestamp <= windowMs
            ) {
                val key = Pair(current.packageName, next.packageName)
                pairCounts.getOrPut(key) { mutableListOf() }.add(next.timestamp)
            }
        }

        return pairCounts.entries
            .sortedByDescending { it.value.size }
            .take(limit)
            .map { (pair, timestamps) ->
                val medianTimestamp = timestamps.sorted().let {
                    it[it.size / 2]
                }
                AppSequence(
                    fromApp = pair.first,
                    toApp = pair.second,
                    count = timestamps.size,
                    typicalTime = timeFormat.format(Date(medianTimestamp))
                )
            }
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
