package com.outsiders.usage.domain.analyzer

import com.outsiders.usage.data.db.dao.DailyUsageDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AppUsageSummary(
    val packageName: String,
    val appName: String,
    val totalMinutes: Int,
    val openCount: Int
)

data class TopAppsResult(
    val apps: List<AppUsageSummary>,
    val totalMinutes: Int
)

class TopAppsAnalyzer(private val dailyUsageDao: DailyUsageDao) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun getTopApps(limit: Int = 10, date: String = dateFormat.format(Date())): Flow<List<AppUsageSummary>> {
        return dailyUsageDao.getUsageForDate(date).map { list ->
            list.take(limit).map {
                AppUsageSummary(
                    packageName = it.packageName,
                    appName = it.appName,
                    totalMinutes = it.totalMinutes,
                    openCount = it.openCount
                )
            }
        }
    }

    suspend fun getTopAppsSnapshot(limit: Int = 10, date: String = dateFormat.format(Date())): TopAppsResult {
        val apps = dailyUsageDao.getUsageForDate(date).first()
        val topApps = apps.take(limit).map {
            AppUsageSummary(
                packageName = it.packageName,
                appName = it.appName,
                totalMinutes = it.totalMinutes,
                openCount = it.openCount
            )
        }
        return TopAppsResult(
            apps = topApps,
            totalMinutes = apps.sumOf { it.totalMinutes }
        )
    }
}
