package com.faiqbaig.metabolic.core.data.repository

import com.faiqbaig.metabolic.core.data.local.WeightLogDao
import com.faiqbaig.metabolic.core.data.local.WeightLogEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class WeightLogRepositoryImpl @Inject constructor(
    private val weightLogDao: WeightLogDao
) : WeightLogRepository {

    override suspend fun logWeight(userId: String, weightKg: Double, heightCm: Double, note: String?) {
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)

        val entry = WeightLogEntity(
            userId = userId,
            weightKg = weightKg,
            bmi = bmi,
            date = LocalDate.now().toString(),
            note = note,
            timestamp = System.currentTimeMillis()
        )
        weightLogDao.insert(entry)
    }

    override suspend fun deleteLog(entry: WeightLogEntity) {
        weightLogDao.delete(entry)
    }

    override fun getWeightHistory(userId: String): Flow<List<WeightLogEntity>> {
        return weightLogDao.getAllLogs(userId)
    }

    override fun getLatestLog(userId: String): Flow<WeightLogEntity?> {
        return weightLogDao.getLatestLog(userId)
    }
}