package com.faiqbaig.metabolic.core.domain.repository

import com.faiqbaig.metabolic.core.data.local.MealLogEntity
import com.faiqbaig.metabolic.core.data.remote.UsdaFood
import kotlinx.coroutines.flow.Flow

interface MealLogRepository {
    // Local DB Operations
    fun getTodaysMeals(userId: String, date: String): Flow<List<MealLogEntity>>
    suspend fun logMeal(mealLog: MealLogEntity)
    suspend fun deleteMealLog(id: String)

    // Remote API Operations
    suspend fun searchFoods(query: String): Result<List<UsdaFood>>
}