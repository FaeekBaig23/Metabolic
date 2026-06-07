package com.faiqbaig.metabolic.feature.profile_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.local.UserProfileEntity
import com.faiqbaig.metabolic.core.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: UserProfileRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<UserProfileEntity?>(null)
    val profileState: StateFlow<UserProfileEntity?> = _profileState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getProfile().collect { profile ->
                if (_profileState.value == null) {
                    _profileState.value = profile
                }
            }
        }
    }

    fun updateField(update: (UserProfileEntity) -> UserProfileEntity) {
        _profileState.value = _profileState.value?.let { update(it) }
    }

    // ── NEW: BMR & Target Recalculation Logic ──
    private fun calculateUpdatedTargets(profile: UserProfileEntity): UserProfileEntity {
        // 1. Calculate BMR (Mifflin-St Jeor Equation)
        val isMale = profile.gender.equals("Male", ignoreCase = true)
        var bmr = (10 * profile.weightKg) + (6.25 * profile.heightCm) - (5 * profile.age)
        bmr += if (isMale) 5 else -161

        // 2. Apply Activity Multiplier
        val activityMultiplier = when (profile.activityLevel.lowercase()) {
            "sedentary" -> 1.2
            "lightly active" -> 1.375
            "moderately active" -> 1.55
            "very active" -> 1.725
            "super active" -> 1.9
            else -> 1.2
        }
        val tdee = bmr * activityMultiplier

        // 3. Adjust for Goal
        val dailyCalorieTarget = when (profile.goal.lowercase()) {
            "lose weight" -> (tdee - 500).toInt()
            "gain weight" -> (tdee + 500).toInt()
            else -> tdee.toInt() // Maintain weight
        }

        // 4. DYNAMIC MACRO CALCULATION BASED ON DIET TYPE
        var dailyProteinTarget = 0
        var dailyCarbsTarget = 0
        var dailyFatTarget = 0

        when (profile.dietType.lowercase()) {
            "keto", "ketogenic" -> {
                // Keto: 70% Fat, 25% Protein, 5% Carbs
                dailyFatTarget = ((dailyCalorieTarget * 0.70) / 9).toInt()
                dailyProteinTarget = ((dailyCalorieTarget * 0.25) / 4).toInt()
                dailyCarbsTarget = ((dailyCalorieTarget * 0.05) / 4).toInt()
            }
            "low carb" -> {
                // Low Carb: 40% Protein, 40% Fat, 20% Carbs
                dailyProteinTarget = ((dailyCalorieTarget * 0.40) / 4).toInt()
                dailyFatTarget = ((dailyCalorieTarget * 0.40) / 9).toInt()
                dailyCarbsTarget = ((dailyCalorieTarget * 0.20) / 4).toInt()
            }
            "high protein" -> {
                // High Protein: 40% Protein, 30% Fat, 30% Carbs
                dailyProteinTarget = ((dailyCalorieTarget * 0.40) / 4).toInt()
                dailyFatTarget = ((dailyCalorieTarget * 0.30) / 9).toInt()
                dailyCarbsTarget = ((dailyCalorieTarget * 0.30) / 4).toInt()
            }
            else -> {
                // Standard / Balanced / Vegetarian / Vegan
                // Baseline: 2g of protein per kg of body weight (Gold standard fitness baseline)
                dailyProteinTarget = (profile.weightKg * 2.0).toInt()
                val proteinCalories = dailyProteinTarget * 4

                // Fat: 25% of total calories
                dailyFatTarget = ((dailyCalorieTarget * 0.25) / 9).toInt()
                val fatCalories = dailyFatTarget * 9

                // Carbs: The remaining calories
                val remainingCalories = dailyCalorieTarget - proteinCalories - fatCalories

                // .coerceAtLeast(0) prevents the app from crashing if a massive deficit creates negative carbs
                dailyCarbsTarget = (remainingCalories / 4).coerceAtLeast(0).toInt()
            }
        }

        // 5. Recalculate BMI
        val heightMeters = profile.heightCm / 100.0
        val calculatedBmi = if (heightMeters > 0) {
            (profile.weightKg / heightMeters.pow(2)).toFloat()
        } else {
            profile.bmi
        }

        return profile.copy(
            dailyCalorieTarget = dailyCalorieTarget,
            dailyProteinTarget = dailyProteinTarget,
            dailyCarbsTarget = dailyCarbsTarget,
            dailyFatTarget = dailyFatTarget,
            bmi = calculatedBmi
        )
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val current = _profileState.value ?: return

        // ── NEW: Intercept and recalculate targets before saving ──
        val updatedProfile = calculateUpdatedTargets(current)

        viewModelScope.launch {
            repository.saveProfile(
                userId = updatedProfile.userId,
                name = updatedProfile.name,
                gender = updatedProfile.gender,
                age = updatedProfile.age,
                weightKg = updatedProfile.weightKg,
                heightCm = updatedProfile.heightCm,
                goal = updatedProfile.goal,
                activityLevel = updatedProfile.activityLevel,
                activityTypes = updatedProfile.activityTypes,
                dietType = updatedProfile.dietType,
                allergies = updatedProfile.allergies,
                medicalConditions = updatedProfile.medicalConditions,
                risks = updatedProfile.risks,
                background = updatedProfile.background,
                dailyCalorieTarget = updatedProfile.dailyCalorieTarget,
                dailyProteinTarget = updatedProfile.dailyProteinTarget,
                dailyCarbsTarget = updatedProfile.dailyCarbsTarget,
                dailyFatTarget = updatedProfile.dailyFatTarget,
                bmi = updatedProfile.bmi
            )
            onSuccess()
        }
    }
}