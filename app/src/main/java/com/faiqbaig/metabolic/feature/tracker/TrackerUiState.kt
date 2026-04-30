package com.faiqbaig.metabolic.feature.tracker

import com.faiqbaig.metabolic.core.data.local.MealLogEntity
import com.faiqbaig.metabolic.core.data.remote.UsdaFood

data class TrackerUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val searchResults: List<UsdaFood> = emptyList(),
    val searchError: String? = null,
    val todaysMeals: List<MealLogEntity> = emptyList()
) {
    // Helper properties so our UI doesn't have to calculate this on the fly!
    val totalCalories: Int get() = todaysMeals.sumOf { it.calories }
    val totalProtein: Int get() = todaysMeals.sumOf { it.protein }
    val totalCarbs: Int get() = todaysMeals.sumOf { it.carbs }
    val totalFat: Int get() = todaysMeals.sumOf { it.fat }

    // Grouping meals makes drawing the "Breakfast", "Lunch", "Dinner" sections incredibly easy
    val mealsByType: Map<String, List<MealLogEntity>> get() = todaysMeals.groupBy { it.mealType }
}