package com.faiqbaig.metabolic.core.data.repository

import com.faiqbaig.metabolic.core.data.local.DietPlanEntity
import com.faiqbaig.metabolic.core.data.local.DietPlanMealEntity
import com.faiqbaig.metabolic.core.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow

data class DietPlanWithMeals(
    val plan: DietPlanEntity,
    val mealsByDay: Map<Int, List<DietPlanMealEntity>>
)

interface DietPlanRepository {
    suspend fun generateAndSavePlan(userId: String, profile: UserProfileEntity): Result<Unit>
    fun getActivePlan(userId: String): Flow<DietPlanWithMeals?>
    suspend fun regeneratePlan(userId: String, profile: UserProfileEntity): Result<Unit>
    suspend fun logMealFromPlan(meal: DietPlanMealEntity, date: String): Result<Unit>
}