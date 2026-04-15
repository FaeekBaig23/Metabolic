package com.faiqbaig.metabolic.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val userId         : String,
    val name           : String        = "",
    val gender         : String        = "",
    val age            : Int           = 0,
    val weightKg       : Float         = 0f,
    val heightCm       : Float         = 0f,
    val goal           : String        = "",
    val activityLevel  : String        = "",
    val activityTypes  : String        = "",  // comma-separated
    val dietType       : String        = "",
    val allergies      : String        = "",
    val medicalConditions: String      = "",
    val risks          : String        = "",
    val background     : String        = "",
    val dailyCalorieTarget: Int        = 0,
    val dailyProteinTarget: Int        = 0,
    val dailyCarbsTarget  : Int        = 0,
    val dailyFatTarget    : Int        = 0,
    val bmi               : Float      = 0f,
    val createdAt         : Long       = System.currentTimeMillis(),
    val updatedAt         : Long       = System.currentTimeMillis()
)