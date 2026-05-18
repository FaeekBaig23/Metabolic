package com.faiqbaig.metabolic.feature.plans

import com.faiqbaig.metabolic.core.data.local.DietPlanMealEntity
import com.faiqbaig.metabolic.core.data.repository.DietPlanWithMeals
import java.time.LocalDate

data class PlansUiState(
    val hasPlan: Boolean = false,
    val isGenerating: Boolean = false,
    val isRegenerateDialogVisible: Boolean = false,
    val plan: DietPlanWithMeals? = null,
    // java.time.DayOfWeek uses 1 (Monday) to 7 (Sunday).
    // We map this to 0-6 to match our dayIndex logic.
    val selectedDayIndex: Int = LocalDate.now().dayOfWeek.value - 1,
    val selectedDayMeals: List<DietPlanMealEntity> = emptyList(),
    val generatedOnLabel: String = "",
    val error: String? = null
)