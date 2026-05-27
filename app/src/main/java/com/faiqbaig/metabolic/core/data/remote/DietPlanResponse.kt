package com.faiqbaig.metabolic.core.data.remote

// ── Moshi imports completely removed ──

data class DietPlanResponse(
    val days: List<DietPlanDay> = emptyList()
)

data class DietPlanDay(
    val dayIndex: Int = 0,
    val meals: List<DietPlanMeal> = emptyList()
)

data class DietPlanMeal(
    val mealType: String = "",
    val foodName: String = "",
    val estimatedWeightG: Double = 0.0,
    val calories: Int = 0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
)