package com.faiqbaig.metabolic.feature.bmi

import com.faiqbaig.metabolic.core.data.local.WeightLogEntity

enum class HistoryFilter {
    WEEK, MONTH, ALL
}

data class BmiUiState(
    val currentBmi: Double = 0.0,
    val bmiCategory: String = "",
    val latestWeightKg: Double = 0.0,
    val weightInputField: String = "",
    val noteField: String = "",
    val historyFilter: HistoryFilter = HistoryFilter.WEEK,
    val chartDataPoints: List<Pair<String, Double>> = emptyList(),
    val weightLogs: List<WeightLogEntity> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null
)