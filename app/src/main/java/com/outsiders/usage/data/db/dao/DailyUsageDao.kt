package com.outsiders.usage.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.outsiders.usage.data.db.entity.DailyUsage
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyUsageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usage: DailyUsage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usages: List<DailyUsage>)

    @Query("SELECT * FROM daily_usage WHERE date = :date ORDER BY totalMinutes DESC")
    fun getUsageForDate(date: String): Flow<List<DailyUsage>>

    @Query("SELECT * FROM daily_usage WHERE date = :date AND packageName = :packageName")
    suspend fun getUsageForPackageAndDate(packageName: String, date: String): DailyUsage?

    @Query("SELECT SUM(totalMinutes) FROM daily_usage WHERE date = :date")
    fun getTotalMinutesForDate(date: String): Flow<Int?>

    @Query("SELECT COUNT(DISTINCT packageName) FROM daily_usage WHERE date = :date AND totalMinutes > 0")
    fun getDistinctAppCountForDate(date: String): Flow<Int?>

    @Query("SELECT COUNT(DISTINCT packageName) FROM daily_usage WHERE date = :date AND totalMinutes > 0")
    fun getAppCountForDate(date: String): Flow<Int>

    @Query("DELETE FROM daily_usage")
    suspend fun deleteAll()

    @Query("SELECT DISTINCT date FROM daily_usage ORDER BY date DESC")
    fun getAllDates(): Flow<List<String>>
}
