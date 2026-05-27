package com.faiqbaig.metabolic.core.data.repository

import com.faiqbaig.metabolic.core.data.local.MealLogDao
import com.faiqbaig.metabolic.core.data.local.MealLogEntity
import com.faiqbaig.metabolic.core.domain.repository.MealLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MealLogRepositoryImpl @Inject constructor(
    private val mealLogDao: MealLogDao
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
}