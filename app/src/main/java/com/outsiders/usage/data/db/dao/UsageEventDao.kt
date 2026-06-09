package com.outsiders.usage.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.outsiders.usage.data.db.entity.UsageEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: UsageEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<UsageEvent>)

    @Query("SELECT * FROM usage_events WHERE timestamp >= :since ORDER BY timestamp ASC")
    fun getEventsSince(since: Long): Flow<List<UsageEvent>>

    @Query("SELECT * FROM usage_events WHERE timestamp >= :since AND eventType = :eventType ORDER BY timestamp ASC")
    fun getEventsSinceByType(since: Long, eventType: String): Flow<List<UsageEvent>>

    @Query("""
        SELECT * FROM usage_events
        WHERE timestamp BETWEEN :startOfDay AND :endOfDay
        ORDER BY timestamp ASC
    """)
    fun getEventsForDay(startOfDay: Long, endOfDay: Long): Flow<List<UsageEvent>>

    @Query("DELETE FROM usage_events")
    suspend fun deleteAll()
}
