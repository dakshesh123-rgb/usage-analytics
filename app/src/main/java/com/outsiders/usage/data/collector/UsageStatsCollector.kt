package com.outsiders.usage.data.collector

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.outsiders.usage.data.db.entity.AppSession
import com.outsiders.usage.data.db.entity.DailyUsage
import com.outsiders.usage.data.db.entity.UsageEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UsageStatsCollector(private val context: Context) {

    private val usageStatsManager: UsageStatsManager? =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private val packageManager: PackageManager = context.packageManager

    fun collectLastHour(): CollectionResult {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3_600_000

        if (usageStatsManager == null) {
            Log.w(TAG, "UsageStatsManager not available")
            return CollectionResult(emptyList(), emptyList(), emptyList())
        }

        val sessions = mutableListOf<AppSession>()
        val dailyUsages = mutableMapOf<String, DailyUsage>()
        val events = mutableListOf<UsageEvent>()

        val usageEvents = usageStatsManager.queryEvents(oneHourAgo, now)

        val eventMap = mutableMapOf<String, MutableList<Long>>()

        while (usageEvents.hasNextEvent()) {
            val event = UsageEvents.Event()
            usageEvents.getNextEvent(event)

            val packageName = event.packageName ?: continue
            val eventType = event.eventType
            val timeStamp = event.timeStamp

            when (eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    events.add(
                        UsageEvent(
                            packageName = packageName,
                            eventType = "OPEN",
                            timestamp = timeStamp
                        )
                    )
                    eventMap.getOrPut(packageName) { mutableListOf() }.add(timeStamp)
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    events.add(
                        UsageEvent(
                            packageName = packageName,
                            eventType = "CLOSE",
                            timestamp = timeStamp
                        )
                    )
                    val timestamps = eventMap[packageName]
                    if (timestamps != null && timestamps.isNotEmpty()) {
                        val startTime = timestamps.removeAt(timestamps.size - 1)
                        val durationMinutes = ((timeStamp - startTime) / 60_000).toInt()
                        val appName = resolveAppName(packageName)
                        sessions.add(
                            AppSession(
                                packageName = packageName,
                                appName = appName,
                                startTime = startTime,
                                endTime = timeStamp,
                                durationMinutes = durationMinutes
                            )
                        )
                        updateDailyUsage(dailyUsages, packageName, appName, timeStamp, durationMinutes)
                    }
                }
            }
        }

        // Handle sessions still in foreground (no CLOSE event)
        for ((pkg, timestamps) in eventMap) {
            for (startTime in timestamps) {
                val durationMinutes = ((now - startTime) / 60_000).toInt()
                val appName = resolveAppName(pkg)
                sessions.add(
                    AppSession(
                        packageName = pkg,
                        appName = appName,
                        startTime = startTime,
                        endTime = now,
                        durationMinutes = durationMinutes
                    )
                )
                updateDailyUsage(dailyUsages, pkg, appName, now, durationMinutes)
            }
        }

        return CollectionResult(sessions, dailyUsages.values.toList(), events)
    }

    private fun resolveAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    private fun updateDailyUsage(
        map: MutableMap<String, DailyUsage>,
        packageName: String,
        appName: String,
        timestamp: Long,
        durationMinutes: Int
    ) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(Date(timestamp))
        val key = "$packageName:$date"
        val existing = map[key]
        if (existing != null) {
            map[key] = existing.copy(
                totalMinutes = existing.totalMinutes + durationMinutes,
                openCount = existing.openCount + 1
            )
        } else {
            map[key] = DailyUsage(
                packageName = packageName,
                appName = appName,
                date = date,
                totalMinutes = durationMinutes,
                openCount = 1
            )
        }
    }

    data class CollectionResult(
        val sessions: List<AppSession>,
        val dailyUsages: List<DailyUsage>,
        val events: List<UsageEvent>
    )

    companion object {
        private const val TAG = "UsageStatsCollector"
    }
}
