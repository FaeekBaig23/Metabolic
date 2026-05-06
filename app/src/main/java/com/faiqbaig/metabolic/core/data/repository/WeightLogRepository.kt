package com.faiqbaig.metabolic.core.data.repository

import com.faiqbaig.metabolic.core.data.local.WeightLogEntity
import kotlinx.coroutines.flow.Flow

interface WeightLogRepository {
    suspend fun logWeight(userId: String, weightKg: Double, heightCm: Double, note: String?)
    suspend fun deleteLog(entry: WeightLogEntity)
    fun getWeightHistory(userId: String): Flow<List<WeightLogEntity>>
    fun getLatestLog(userId: String): Flow<WeightLogEntity?>
}