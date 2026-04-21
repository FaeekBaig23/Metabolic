package com.faiqbaig.metabolic.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "meal_logs")
data class MealLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val date: String, // Format: "yyyy-MM-dd"
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val foodName: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val servingQty: Float,
    val servingUnit: String,
    val timestamp: Long = System.currentTimeMillis()
)