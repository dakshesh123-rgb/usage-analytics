package com.outsiders.usage.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.outsiders.usage.data.db.dao.AppSessionDao
import com.outsiders.usage.data.db.dao.DailyUsageDao
import com.outsiders.usage.data.db.dao.UsageEventDao
import com.outsiders.usage.data.db.entity.AppSession
import com.outsiders.usage.data.db.entity.DailyUsage
import com.outsiders.usage.data.db.entity.UsageEvent

@Database(
    entities = [AppSession::class, DailyUsage::class, UsageEvent::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appSessionDao(): AppSessionDao
    abstract fun dailyUsageDao(): DailyUsageDao
    abstract fun usageEventDao(): UsageEventDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "usage_analytics.db"
            ).build()
        }
    }
}
