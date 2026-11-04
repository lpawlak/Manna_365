package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_plan_state")
data class UserPlanStateEntity(
    @PrimaryKey val planId: String,
    val isActive: Boolean = true,
    val startDate: Long = System.currentTimeMillis(),
    val currentDay: Int = 1,
    val lastReadDateMillis: Long = System.currentTimeMillis(),
    val streakDays: Int = 1,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val reminderEnabled: Boolean = false
)
