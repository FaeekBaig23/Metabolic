package com.faiqbaig.metabolic.feature.dashboard

import com.faiqbaig.metabolic.core.data.local.MealLogEntity

data class DashboardUiState(
    val userName: String = "",
    val goal: String = "",
    val greeting: String = "",

    val todaysMeals: List<MealLogEntity> = emptyList(),

    // ── Targets (Pulled from Firebase Profile) ──
    val dailyCalorieTarget: Int = 2000,
    val proteinTarget: Int = 0,
    val carbsTarget: Int = 0,
    val fatTarget: Int = 0,

    // ── Consumed (Live from Room Database) ──
    val totalCalories: Int = 0,
    val totalProtein: Int = 0,
    val totalCarbs: Int = 0,
    val totalFat: Int = 0,

    // ── Metrics & Extras ──
    val bmi: Double = 0.0,
    val waterGlasses: Int = 0,
    val streak: Int = 0,

    // ── UI State ──
    val isLoading: Boolean = true
) {
    // Computed properties for the UI to use directly
    val caloriesRemaining: Int
        get() = (dailyCalorieTarget - totalCalories).coerceAtLeast(0)

    val calorieProgressFraction: Float
        get() = if (dailyCalorieTarget > 0) {
            (totalCalories.toFloat() / dailyCalorieTarget.toFloat()).coerceIn(0f, 1f)
        } else 0f
}