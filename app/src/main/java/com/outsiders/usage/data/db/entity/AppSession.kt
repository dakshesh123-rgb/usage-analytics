package com.outsiders.usage.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_sessions",
    indices = [Index(value = ["packageName"])]
)
data class AppSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val startTime: Long,  // epoch millis
    val endTime: Long? = null,
    val durationMinutes: Int = 0
)
