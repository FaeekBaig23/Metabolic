package com.faiqbaig.metabolic.core.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DietPlanResponse(
    val days: List<DietPlanDay>
)

@JsonClass(generateAdapter = true)
data class DietPlanDay(
    val dayIndex: Int,
    val meals: List<DietPlanMeal>
)

@JsonClass(generateAdapter = true)
data class DietPlanMeal(
    val mealType: String,
    val foodName: String,
    val estimatedWeightG: Double,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)