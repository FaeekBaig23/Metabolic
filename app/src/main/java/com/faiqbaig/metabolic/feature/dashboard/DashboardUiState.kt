package com.faiqbaig.metabolic.feature.dashboard

// Stub for Step 6 MealLog
data class DummyMealLog(val id: String = "")

data class DashboardUiState(
    val userName: String = "",
    val goal: String = "",
    val greeting: String = "",

    // Targets
    val dailyCalorieTarget: Int = 2000,
    val proteinTarget: Int = 0,
    val carbsTarget: Int = 0,
    val fatTarget: Int = 0,

    // Consumed (Stubbed to 0 for Step 5)
    val caloriesConsumed: Int = 0,
    val proteinConsumed: Int = 0,
    val carbsConsumed: Int = 0,
    val fatConsumed: Int = 0,

    // Metrics & Extras
    val bmi: Double = 0.0,
    val waterGlasses: Int = 0,
    val streak: Int = 0,

    // Lists
    val todaysMeals: List<DummyMealLog> = emptyList(),

    // UI State
    val isLoading: Boolean = true
) {
    // Computed properties for the UI to use directly
    val caloriesRemaining: Int
        get() = (dailyCalorieTarget - caloriesConsumed).coerceAtLeast(0)

    val calorieProgressFraction: Float
        get() = if (dailyCalorieTarget > 0) {
            (caloriesConsumed.toFloat() / dailyCalorieTarget.toFloat()).coerceIn(0f, 1f)
        } else 0f
}