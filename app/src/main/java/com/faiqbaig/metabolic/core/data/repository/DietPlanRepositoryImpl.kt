package com.faiqbaig.metabolic.core.data.repository

import com.faiqbaig.metabolic.core.data.local.DietPlanDao
import com.faiqbaig.metabolic.core.data.local.DietPlanEntity
import com.faiqbaig.metabolic.core.data.local.DietPlanMealEntity
import com.faiqbaig.metabolic.core.data.local.MealLogDao
import com.faiqbaig.metabolic.core.data.local.MealLogEntity
import com.faiqbaig.metabolic.core.data.local.UserProfileEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class DietPlanRepositoryImpl @Inject constructor(
    private val dietPlanDao: DietPlanDao,
    private val mealLogDao: MealLogDao,
    private val geminiRepository: GeminiRepository
) : DietPlanRepository {

    override suspend fun generateAndSavePlan(userId: String, profile: UserProfileEntity): Result<Unit> {
        return try {
            val generatedPlanResponse = geminiRepository.generateDietPlan(profile).getOrThrow()

            val today = LocalDate.now()
            val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()

            val planEntity = DietPlanEntity(
                userId = userId,
                generatedAt = System.currentTimeMillis(),
                weekStartDate = weekStart
            )
            val planId = dietPlanDao.insertPlan(planEntity).toInt()

            val mealEntities = generatedPlanResponse.days.flatMap { day ->
                day.meals.map { meal ->
                    DietPlanMealEntity(
                        planId = planId,
                        dayIndex = day.dayIndex,
                        mealType = meal.mealType,
                        foodName = meal.foodName,
                        calories = meal.calories,
                        protein = meal.protein,
                        carbs = meal.carbs,
                        fat = meal.fat,
                        estimatedWeightG = meal.estimatedWeightG
                    )
                }
            }

            dietPlanDao.insertMeals(mealEntities)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // THE FIX: Safely flattening the relational Flow instead of blocking it with collect
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getActivePlan(userId: String): Flow<DietPlanWithMeals?> {
        return dietPlanDao.getActivePlan(userId).flatMapLatest { plan ->
            if (plan == null) {
                flowOf(null)
            } else {
                dietPlanDao.getMealsForPlan(plan.id).map { meals ->
                    DietPlanWithMeals(
                        plan = plan,
                        mealsByDay = meals.groupBy { it.dayIndex }
                    )
                }
            }
        }
    }

    override suspend fun regeneratePlan(userId: String, profile: UserProfileEntity): Result<Unit> {
        return try {
            dietPlanDao.deleteAllPlansForUser(userId)
            generateAndSavePlan(userId, profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logMealFromPlan(meal: DietPlanMealEntity, date: String): Result<Unit> {
        return try {
            val mealLog = MealLogEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = "CURRENT_USER_ID", // TODO: Fetch from Session Manager
                date = date,
                mealType = meal.mealType,
                foodName = meal.foodName,
                calories = meal.calories,
                protein = meal.protein.toInt(),     // Cast Double to Int
                carbs = meal.carbs.toInt(),         // Cast Double to Int
                fat = meal.fat.toInt(),             // Cast Double to Int
                servingQty = meal.estimatedWeightG.toFloat(), // Map to your existing servingQty field
                servingUnit = "g",                  // Provide default unit
                timestamp = System.currentTimeMillis()
            )
            mealLogDao.insertMealLog(mealLog)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteActivePlan(userId: String) {
        dietPlanDao.deleteAllPlansForUser(userId)
    }
}