package com.faiqbaig.metabolic.feature.tracker

import com.faiqbaig.metabolic.core.data.local.MealLogEntity

data class TrackerUiState(
    val todaysMeals: List<MealLogEntity> = emptyList()
) {
    // Helper properties so our UI doesn't have to calculate this on the fly!
    val totalCalories: Int get() = todaysMeals.sumOf { it.calories }
    val totalProtein: Int get() = todaysMeals.sumOf { it.protein.toInt() } // Cast to Int if your entity stores these as Doubles/Floats
    val totalCarbs: Int get() = todaysMeals.sumOf { it.carbs.toInt() }
    val totalFat: Int get() = todaysMeals.sumOf { it.fat.toInt() }

    // Grouping meals makes drawing the "Breakfast", "Lunch", "Dinner" sections incredibly easy
    val mealsByType: Map<String, List<MealLogEntity>> get() = todaysMeals.groupBy { it.mealType }
}