package com.faiqbaig.metabolic.core.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UsdaSearchResponse(
    val totalHits: Int,
    val foods: List<UsdaFood>
)

@JsonClass(generateAdapter = true)
data class UsdaFood(
    val fdcId: Int,
    val description: String,
    val brandOwner: String?,
    val servingSize: Float?,
    val servingSizeUnit: String?,
    val foodNutrients: List<UsdaNutrient>
)

@JsonClass(generateAdapter = true)
data class UsdaNutrient(
    val nutrientId: Int, // We will use IDs to filter Protein (1003), Fat (1004), Carbs (1005), Calories (1008)
    val nutrientName: String,
    val value: Float,
    val unitName: String
)