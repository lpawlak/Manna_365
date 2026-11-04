package com.example.data.entity

import androidx.room.Entity

@Entity(
    tableName = "reading_progress",
    primaryKeys = ["planId", "dayNumber", "bookCode", "chapter"]
)
data class ReadingProgressEntity(
    val planId: String,
    val dayNumber: Int,
    val bookCode: String,
    val chapter: Int,
    val isCompleted: Boolean = true,
    val completedAt: Long = System.currentTimeMillis()
)
