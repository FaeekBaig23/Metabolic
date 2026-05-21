package com.faiqbaig.metabolic.feature.profile_view

import com.faiqbaig.metabolic.core.data.local.UserProfileEntity

data class ProfileUiState(
    // Section A: Profile Data
    val userProfile: UserProfileEntity? = null,
    val isLoading: Boolean = true,

    // Section B: App Settings
    val dailyWaterTargetMl: Int = 2500,
    val weightUnit: String = "kg",
    val heightUnit: String = "cm",
    val profileImageUri: String? = null, // NEW
    val latestWeightKg: Float? = null,   // NEW (Pulled from weight logs)
    val latestBmi: Float? = null,
    val mealRemindersEnabled: Boolean = false,
    val hydrationRemindersEnabled: Boolean = false,
    val weightRemindersEnabled: Boolean = false,

    // Section C: Account Actions
    val isPasswordResetSent: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val error: String? = null
)