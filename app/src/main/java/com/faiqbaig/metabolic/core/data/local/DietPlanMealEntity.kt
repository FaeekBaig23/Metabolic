package com.faiqbaig.metabolic.core.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "diet_plan_meals",
    foreignKeys = [
        ForeignKey(
            entity = DietPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("planId")] // Good practice to index foreign keys
)
data class DietPlanMealEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val planId: Int, // Foreign key linking to DietPlanEntity
    val dayIndex: Int, // 0 = Monday ... 6 = Sunday
    val mealType: String, // "Breakfast" | "Lunch" | "Dinner" | "Snack"
    val foodName: String,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val estimatedWeightG: Double
)