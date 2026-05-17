package com.faiqbaig.metabolic.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diet_plans")
data class DietPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val generatedAt: Long,
    val weekStartDate: String // Format: "yyyy-MM-dd" (Monday of the plan week)
)