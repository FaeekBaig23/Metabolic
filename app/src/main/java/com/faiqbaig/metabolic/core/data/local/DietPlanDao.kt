package com.faiqbaig.metabolic.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DietPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: DietPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeals(meals: List<DietPlanMealEntity>)

    @Query("SELECT * FROM diet_plans WHERE userId = :userId ORDER BY generatedAt DESC LIMIT 1")
    fun getActivePlan(userId: String): Flow<DietPlanEntity?>

    @Query("SELECT * FROM diet_plan_meals WHERE planId = :planId ORDER BY dayIndex ASC")
    fun getMealsForPlan(planId: Int): Flow<List<DietPlanMealEntity>>

    @Query("DELETE FROM diet_plans WHERE id = :planId")
    suspend fun deletePlan(planId: Int) // Cascades to meals automatically

    @Query("DELETE FROM diet_plans WHERE userId = :userId")
    suspend fun deleteAllPlansForUser(userId: String)
}