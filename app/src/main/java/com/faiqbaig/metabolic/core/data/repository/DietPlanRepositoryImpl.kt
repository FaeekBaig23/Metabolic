package com.faiqbaig.metabolic.core.data.repository

import com.faiqbaig.metabolic.core.data.local.DietPlanDao
import com.faiqbaig.metabolic.core.data.local.DietPlanEntity
import com.faiqbaig.metabolic.core.data.local.DietPlanMealEntity
import com.faiqbaig.metabolic.core.data.local.MealLogDao
import com.faiqbaig.metabolic.core.data.local.MealLogEntity
import com.faiqbaig.metabolic.core.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow
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
            // 1. Fetch AI generated plan from Gemini (we will implement this in GeminiRepository next)
            val generatedPlanResponse = geminiRepository.generateDietPlan(profile).getOrThrow()

            // 2. Calculate the Monday of the current week
            val today = LocalDate.now()
            val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()

            // 3. Create and insert the master plan entity
            val planEntity = DietPlanEntity(
                userId = userId,
                generatedAt = System.currentTimeMillis(),
                weekStartDate = weekStart
            )
            val planId = dietPlanDao.insertPlan(planEntity).toInt()

            // 4. Map the Gemini JSON response to Room entities
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

            // 5. Insert all meals
            dietPlanDao.insertMeals(mealEntities)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getActivePlan(userId: String): Flow<DietPlanWithMeals?> {
        return dietPlanDao.getActivePlan(userId).map { plan ->
            if (plan == null) return@map null

            // Collect the meals for this plan
            var mealsByDay: Map<Int, List<DietPlanMealEntity>> = emptyMap()
            dietPlanDao.getMealsForPlan(plan.id).collect { meals ->
                mealsByDay = meals.groupBy { it.dayIndex }
            }

            DietPlanWithMeals(plan = plan, mealsByDay = mealsByDay)
        }
    }

    override suspend fun regeneratePlan(userId: String, profile: UserProfileEntity): Result<Unit> {
        return try {
            // Cascade delete handles removing the old meals automatically
            dietPlanDao.deleteAllPlansForUser(userId)
            generateAndSavePlan(userId, profile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logMealFromPlan(meal: DietPlanMealEntity, date: String): Result<Unit> {
        return try {
            val mealLog = MealLogEntity(
                userId = "CURRENT_USER_ID", // TODO: Fetch from Auth/Session manager
                date = date,
                mealType = meal.mealType,
                foodName = meal.foodName,
                calories = meal.calories,
                protein = meal.protein.toInt(), // Cast to Int to match current entity
                carbs = meal.carbs.toInt(),     // Cast to Int
                fat = meal.fat.toInt(),         // Cast to Int
                servingQty = meal.estimatedWeightG.toFloat(), // Map to servingQty
                servingUnit = "g",                  // Provide a default string
                timestamp = System.currentTimeMillis()
            )
            mealLogDao.insertMealLog(mealLog)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}