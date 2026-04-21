package com.faiqbaig.metabolic.core.data.repository

import com.faiqbaig.metabolic.core.data.local.MealLogDao
import com.faiqbaig.metabolic.core.data.local.MealLogEntity
import com.faiqbaig.metabolic.core.data.remote.UsdaApi
import com.faiqbaig.metabolic.core.data.remote.UsdaFood
import com.faiqbaig.metabolic.core.domain.repository.MealLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MealLogRepositoryImpl @Inject constructor(
    private val mealLogDao: MealLogDao,
    private val usdaApi: UsdaApi
) : MealLogRepository {

    // ─── LOCAL DATABASE (ROOM) ───────────────────────────────────────────

    override fun getTodaysMeals(userId: String, date: String): Flow<List<MealLogEntity>> {
        return mealLogDao.getTodaysMeals(userId, date)
    }

    override suspend fun logMeal(mealLog: MealLogEntity) {
        mealLogDao.insertMealLog(mealLog)
    }

    override suspend fun deleteMealLog(id: String) {
        mealLogDao.deleteMealLogById(id)
    }

    // ─── REMOTE NETWORK (USDA API) ───────────────────────────────────────

    override suspend fun searchFoods(query: String): Result<List<UsdaFood>> {
        return try {
            val response = usdaApi.searchFoods(query = query)

            // The USDA DB is massive. Sometimes it returns foods with empty nutrient data.
            // Let's filter out anything that doesn't have at least some macro information to keep our UI clean.
            val validFoods = response.foods.filter { food ->
                food.foodNutrients.isNotEmpty()
            }

            Result.success(validFoods)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}