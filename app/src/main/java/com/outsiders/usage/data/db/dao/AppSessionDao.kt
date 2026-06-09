package com.outsiders.usage.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.outsiders.usage.data.db.entity.AppSession
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: AppSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<AppSession>)

    @Query("SELECT * FROM app_sessions WHERE packageName = :packageName ORDER BY startTime DESC")
    fun getSessionsForPackage(packageName: String): Flow<List<AppSession>>

    @Query("SELECT * FROM app_sessions WHERE startTime >= :since ORDER BY startTime DESC")
    fun getSessionsSince(since: Long): Flow<List<AppSession>>

    @Query("""
        SELECT * FROM app_sessions
        WHERE date(startTime / 1000, 'unixepoch') = :date
        ORDER BY startTime DESC
    """)
    fun getSessionsForDate(date: String): Flow<List<AppSession>>

    @Query("SELECT SUM(durationMinutes) FROM app_sessions WHERE date(startTime / 1000, 'unixepoch') = :date")
    fun getTotalDurationForDate(date: String): Flow<Int?>

    @Query("DELETE FROM app_sessions")
    suspend fun deleteAll()
}
