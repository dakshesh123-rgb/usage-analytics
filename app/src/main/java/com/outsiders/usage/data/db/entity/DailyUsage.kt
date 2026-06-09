package com.outsiders.usage.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.UniqueConstraint

@Entity(
    tableName = "daily_usage",
    indices = [
        Index(value = ["packageName"]),
        Index(value = ["date"])
    ],
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["packageName", "date"])
    ]
)
data class DailyUsage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val appName: String,
    val date: String,  // "YYYY-MM-DD"
    val totalMinutes: Int = 0,
    val openCount: Int = 0
)
