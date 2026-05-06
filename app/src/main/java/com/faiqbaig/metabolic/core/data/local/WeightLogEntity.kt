package com.faiqbaig.metabolic.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val weightKg: Double,
    val bmi: Double,
    val date: String,
    val note: String?,
    val timestamp: Long
)