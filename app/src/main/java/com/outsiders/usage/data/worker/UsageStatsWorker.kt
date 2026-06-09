package com.outsiders.usage.data.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.outsiders.usage.data.collector.UsageStatsCollector
import com.outsiders.usage.data.db.AppDatabase
import java.util.concurrent.TimeUnit

class UsageStatsWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getInstance(applicationContext)
            val collector = UsageStatsCollector(applicationContext)

            val result = collector.collectLastHour()

            // Insert collected data
            db.appSessionDao().insertAll(result.sessions)
            db.dailyUsageDao().insertAll(result.dailyUsages)
            db.usageEventDao().insertAll(result.events)

            Log.i(TAG, "Collected ${result.sessions.size} sessions, " +
                    "${result.dailyUsages.size} daily records, ${result.events.size} events")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Collection failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "UsageStatsWorker"
        private const val INTERVAL_MINUTES = 15L

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<UsageStatsWorker>(
                INTERVAL_MINUTES, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "usage_stats_collection",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("usage_stats_collection")
            Log.i(TAG, "Usage stats collection cancelled")
        }
    }
}
