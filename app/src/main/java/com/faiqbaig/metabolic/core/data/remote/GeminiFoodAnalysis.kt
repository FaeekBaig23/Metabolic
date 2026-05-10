package com.faiqbaig.metabolic.core.data.remote

data class GeminiFoodAnalysis(
    val foodName: String,
    val estimatedWeightG: Double,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)