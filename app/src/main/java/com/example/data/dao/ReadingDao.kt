package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.entity.ReadingProgressEntity
import com.example.data.entity.UserPlanStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingDao {

    @Query("SELECT * FROM reading_progress WHERE planId = :planId AND isCompleted = 1")
    fun getCompletedPassages(planId: String): Flow<List<ReadingProgressEntity>>

    @Query("SELECT * FROM reading_progress WHERE planId = :planId AND dayNumber = :dayNumber AND isCompleted = 1")
    fun getCompletedPassagesForDay(planId: String, dayNumber: Int): Flow<List<ReadingProgressEntity>>

    @Query("SELECT * FROM reading_progress WHERE isCompleted = 1")
    fun getAllCompletedPassages(): Flow<List<ReadingProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ReadingProgressEntity)

    @Query("DELETE FROM reading_progress WHERE planId = :planId AND dayNumber = :dayNumber AND bookCode = :bookCode AND chapter = :chapter")
    suspend fun deleteProgress(planId: String, dayNumber: Int, bookCode: String, chapter: Int)

    @Query("SELECT * FROM user_plan_state WHERE isActive = 1 LIMIT 1")
    fun getActivePlanState(): Flow<UserPlanStateEntity?>

    @Query("SELECT * FROM user_plan_state WHERE planId = :planId LIMIT 1")
    fun getPlanState(planId: String): Flow<UserPlanStateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlanState(state: UserPlanStateEntity)

    @Query("UPDATE user_plan_state SET isActive = 0")
    suspend fun deactivateAllPlans()

    @Transaction
    suspend fun setActivePlan(state: UserPlanStateEntity) {
        deactivateAllPlans()
        insertOrUpdatePlanState(state.copy(isActive = true))
    }

    @Query("DELETE FROM reading_progress WHERE planId = :planId")
    suspend fun clearPlanProgress(planId: String)
}
