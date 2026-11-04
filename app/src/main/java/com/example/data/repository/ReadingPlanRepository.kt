package com.example.data.repository

import com.example.data.dao.ReadingDao
import com.example.data.entity.ReadingProgressEntity
import com.example.data.entity.UserPlanStateEntity
import com.example.model.BibleData
import com.example.model.DayPlan
import com.example.model.PlanDefinition
import com.example.model.ReadingPassage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar

class ReadingPlanRepository(private val readingDao: ReadingDao) {

    val availablePlans: List<PlanDefinition> = BibleData.availablePlans

    val activePlanState: Flow<UserPlanStateEntity> = readingDao.getActivePlanState().map { state ->
        state ?: UserPlanStateEntity(
            planId = "plan_bible_365",
            isActive = true,
            startDate = System.currentTimeMillis(),
            currentDay = 1,
            lastReadDateMillis = System.currentTimeMillis(),
            streakDays = 1,
            reminderHour = 8,
            reminderMinute = 0,
            reminderEnabled = false
        )
    }

    fun getDayPlan(planId: String, dayNumber: Int): DayPlan {
        return BibleData.generateDayPlan(planId, dayNumber)
    }

    fun getCompletedPassagesForPlan(planId: String): Flow<List<ReadingProgressEntity>> {
        return readingDao.getCompletedPassages(planId)
    }

    fun getCompletedPassagesForDay(planId: String, dayNumber: Int): Flow<List<ReadingProgressEntity>> {
        return readingDao.getCompletedPassagesForDay(planId, dayNumber)
    }

    fun getAllCompletedPassages(): Flow<List<ReadingProgressEntity>> {
        return readingDao.getAllCompletedPassages()
    }

    suspend fun togglePassageCompletion(
        planId: String,
        dayNumber: Int,
        passage: ReadingPassage,
        currentlyCompleted: Boolean
    ) {
        if (currentlyCompleted) {
            readingDao.deleteProgress(
                planId = planId,
                dayNumber = dayNumber,
                bookCode = passage.bookCode,
                chapter = passage.chapter
            )
        } else {
            val progress = ReadingProgressEntity(
                planId = planId,
                dayNumber = dayNumber,
                bookCode = passage.bookCode,
                chapter = passage.chapter,
                isCompleted = true,
                completedAt = System.currentTimeMillis()
            )
            readingDao.insertProgress(progress)

            // Update user state if needed
            val currentState = readingDao.getPlanState(planId).firstOrNull() ?: UserPlanStateEntity(planId = planId)
            val now = System.currentTimeMillis()
            val newStreak = calculateNewStreak(currentState.lastReadDateMillis, currentState.streakDays)
            readingDao.insertOrUpdatePlanState(
                currentState.copy(
                    lastReadDateMillis = now,
                    streakDays = newStreak
                )
            )
        }
    }

    suspend fun markDayCompleted(planId: String, dayPlan: DayPlan) {
        val now = System.currentTimeMillis()
        dayPlan.passages.forEach { passage ->
            readingDao.insertProgress(
                ReadingProgressEntity(
                    planId = planId,
                    dayNumber = dayPlan.dayNumber,
                    bookCode = passage.bookCode,
                    chapter = passage.chapter,
                    isCompleted = true,
                    completedAt = now
                )
            )
        }
        val currentState = readingDao.getPlanState(planId).firstOrNull() ?: UserPlanStateEntity(planId = planId)
        val newStreak = calculateNewStreak(currentState.lastReadDateMillis, currentState.streakDays)
        readingDao.insertOrUpdatePlanState(
            currentState.copy(
                lastReadDateMillis = now,
                streakDays = newStreak,
                currentDay = (dayPlan.dayNumber + 1).coerceAtMost(
                    availablePlans.firstOrNull { it.id == planId }?.totalDays ?: 365
                )
            )
        )
    }

    suspend fun setCurrentDay(planId: String, newDay: Int) {
        val currentState = readingDao.getPlanState(planId).firstOrNull() ?: UserPlanStateEntity(planId = planId)
        readingDao.insertOrUpdatePlanState(
            currentState.copy(currentDay = newDay.coerceAtLeast(1))
        )
    }

    suspend fun selectPlan(planId: String) {
        val existing = readingDao.getPlanState(planId).firstOrNull()
        val newState = existing?.copy(isActive = true) ?: UserPlanStateEntity(
            planId = planId,
            isActive = true,
            startDate = System.currentTimeMillis(),
            currentDay = 1,
            lastReadDateMillis = System.currentTimeMillis(),
            streakDays = 0
        )
        readingDao.setActivePlan(newState)
    }

    suspend fun updateReminderSettings(enabled: Boolean, hour: Int, minute: Int) {
        val activeState = readingDao.getActivePlanState().firstOrNull() ?: UserPlanStateEntity(planId = "plan_bible_365")
        readingDao.insertOrUpdatePlanState(
            activeState.copy(
                reminderEnabled = enabled,
                reminderHour = hour,
                reminderMinute = minute
            )
        )
    }

    suspend fun resetPlan(planId: String) {
        readingDao.clearPlanProgress(planId)
        val existing = readingDao.getPlanState(planId).firstOrNull() ?: UserPlanStateEntity(planId = planId)
        readingDao.insertOrUpdatePlanState(
            existing.copy(
                currentDay = 1,
                startDate = System.currentTimeMillis(),
                lastReadDateMillis = 0L,
                streakDays = 0
            )
        )
    }

    private fun calculateNewStreak(lastReadMillis: Long, currentStreak: Int): Int {
        if (lastReadMillis == 0L) return 1
        val lastCal = Calendar.getInstance().apply { timeInMillis = lastReadMillis }
        val nowCal = Calendar.getInstance()

        val isSameDay = lastCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR) &&
                lastCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR)
        if (isSameDay) return currentStreak.coerceAtLeast(1)

        val diffDays = (nowCal.timeInMillis - lastCal.timeInMillis) / (1000 * 60 * 60 * 24)
        return if (diffDays <= 1) {
            currentStreak + 1
        } else {
            1
        }
    }
}
