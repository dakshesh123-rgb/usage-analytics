package com.outsiders.usage.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usage_events",
    indices = [Index(value = ["timestamp"])]
)
data class UsageEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val eventType: String,  // OPEN / CLOSE / LOCK / UNLOCK
    val timestamp: Long  // epoch millis
)
