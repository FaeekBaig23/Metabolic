package com.faiqbaig.metabolic.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

// ─── Step enum ───────────────────────────────────────────────────────────────

enum class ProfileStep(val index: Int, val title: String) {
    BASIC_INFO(0, "About You"),
    HEALTH_GOAL(1, "Your Goal"),
    ACTIVITY(2, "Activity"),
    DIET(3, "Diet"),
    HEALTH_BACKGROUND(4, "Health")
}

// ─── UI State ─────────────────────────────────────────────────────────────────

data class ProfileSetupUiState(
    val currentStep: ProfileStep = ProfileStep.BASIC_INFO,

    // Step 1 — Basic Info
    val name: String = "",
    val gender: String = "",          // "Male" | "Female" | "Other"
    val age: String = "",
    val weightKg: String = "",
    val heightCm: String = "",

    // Step 2 — Health Goal
    val goal: String = "",            // "Lose Weight" | "Gain Weight" | "Build Muscle" | "Athletics" | "Maintenance"

    // Step 3 — Activity
    val activityLevel: String = "",   // "Sedentary" | "Lightly Active" | "Moderately Active" | "Very Active"
    val activityTypes: Set<String> = emptySet(), // "Gym" | "Yoga" | "Sports" | "Cardio" | "Home Workouts" | "Other"

    // Step 4 — Diet
    val dietType: String = "",        // "No Preference" | "Vegetarian" | "Vegan" | "Keto" | "Paleo" | "Mediterranean"
    val allergies: Set<String> = emptySet(), // "Gluten" | "Dairy" | "Nuts" | "Eggs" | "Soy" | "Shellfish"

    // Step 5 — Health Background
    val medicalConditions: String = "",
    val healthRisks: String = "",
    val additionalInfo: String = "",

    // Save state
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isSaveComplete: Boolean = false
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val repository: UserProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    // ── Navigation ──────────────────────────────────────────────────────────

    fun goToNextStep() {
        val steps = ProfileStep.entries
        val current = _uiState.value.currentStep
        val nextIndex = current.index + 1
        if (nextIndex < steps.size) {
            _uiState.update { it.copy(currentStep = steps[nextIndex]) }
        }
    }

    fun goToPreviousStep() {
        val steps = ProfileStep.entries
        val current = _uiState.value.currentStep
        val prevIndex = current.index - 1
        if (prevIndex >= 0) {
            _uiState.update { it.copy(currentStep = steps[prevIndex]) }
        }
    }

    // ── Field updates ────────────────────────────────────────────────────────

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value) }
    fun onGenderChange(value: String) = _uiState.update { it.copy(gender = value) }
    fun onAgeChange(value: String) = _uiState.update { it.copy(age = value) }
    fun onWeightChange(value: String) = _uiState.update { it.copy(weightKg = value) }
    fun onHeightChange(value: String) = _uiState.update { it.copy(heightCm = value) }

    fun onGoalChange(value: String) = _uiState.update { it.copy(goal = value) }

    fun onActivityLevelChange(value: String) = _uiState.update { it.copy(activityLevel = value) }
    fun onActivityTypeToggle(type: String) = _uiState.update {
        val updated = if (type in it.activityTypes) it.activityTypes - type else it.activityTypes + type
        it.copy(activityTypes = updated)
    }

    fun onDietTypeChange(value: String) = _uiState.update { it.copy(dietType = value) }
    fun onAllergyToggle(allergy: String) = _uiState.update {
        val updated = if (allergy in it.allergies) it.allergies - allergy else it.allergies + allergy
        it.copy(allergies = updated)
    }

    fun onMedicalConditionsChange(value: String) = _uiState.update { it.copy(medicalConditions = value) }
    fun onHealthRisksChange(value: String) = _uiState.update { it.copy(healthRisks = value) }
    fun onAdditionalInfoChange(value: String) = _uiState.update { it.copy(additionalInfo = value) }

    fun clearError() = _uiState.update { it.copy(saveError = null) }

    // ── Validation ───────────────────────────────────────────────────────────

    fun isCurrentStepValid(): Boolean {
        val s = _uiState.value
        return when (s.currentStep) {
            ProfileStep.BASIC_INFO -> s.name.isNotBlank() && s.gender.isNotBlank()
                    && s.age.isNotBlank() && s.weightKg.isNotBlank() && s.heightCm.isNotBlank()
            ProfileStep.HEALTH_GOAL -> s.goal.isNotBlank()
            ProfileStep.ACTIVITY -> s.activityLevel.isNotBlank()
            ProfileStep.DIET -> s.dietType.isNotBlank()
            ProfileStep.HEALTH_BACKGROUND -> true // optional
        }
    }

    // ── Calculations ─────────────────────────────────────────────────────────

    private fun calculateBmi(weightKg: Double, heightCm: Double): Double {
        val heightM = heightCm / 100.0
        return (weightKg / (heightM * heightM) * 10.0).roundToInt() / 10.0
    }

    /**
     * Harris-Benedict equation (revised Mifflin–St Jeor)
     * Returns MacroResult(calories, protein, carbs, fat)
     */
    private fun calculateMacros(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        gender: String,
        activityLevel: String,
        goal: String
    ): MacroResult {
        val bmr = when (gender.lowercase()) {
            "female" -> (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161
            else     -> (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
        }
        val activityMultiplier = when (activityLevel) {
            "Sedentary"          -> 1.2
            "Lightly Active"     -> 1.375
            "Moderately Active"  -> 1.55
            "Very Active"        -> 1.725
            else                 -> 1.375
        }
        var tdee = bmr * activityMultiplier
        tdee = when (goal) {
            "Lose Weight"  -> tdee - 500
            "Gain Weight"  -> tdee + 400
            "Build Muscle" -> tdee + 250
            else           -> tdee
        }
        val calories = tdee.roundToInt()
        val protein  = (weightKg * 2.0).roundToInt()            // 2g/kg
        val fat      = ((calories * 0.25) / 9.0).roundToInt()   // 25% cals
        val carbs    = ((calories - (protein * 4) - (fat * 9)) / 4.0).roundToInt()

        return MacroResult(
            calories = calories,
            protein = protein,
            carbs = carbs.coerceAtLeast(0),
            fat = fat
        )
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    fun saveProfile() {
        val s = _uiState.value
        val userId = auth.currentUser?.uid ?: return

        val weight = s.weightKg.toDoubleOrNull() ?: return
        val height = s.heightCm.toDoubleOrNull() ?: return
        val age    = s.age.toIntOrNull() ?: return

        val bmi = calculateBmi(weight, height)
        val (calories, protein, carbs, fat) = calculateMacros(
            weight, height, age, s.gender, s.activityLevel, s.goal
        )

        _uiState.update { it.copy(isSaving = true, saveError = null) }

        viewModelScope.launch {
            try {
                repository.saveProfile(
                    userId          = userId,
                    name            = s.name.trim(),
                    gender          = s.gender,
                    age             = age,
                    weightKg        = weight.toFloat(), // Converted to Float for Room DB
                    heightCm        = height.toFloat(), // Converted to Float for Room DB
                    goal            = s.goal,
                    activityLevel   = s.activityLevel,
                    activityTypes   = s.activityTypes.joinToString(","),
                    dietType        = s.dietType,
                    allergies       = s.allergies.joinToString(","),
                    medicalConditions = s.medicalConditions.trim(),
                    risks           = s.healthRisks.trim(),
                    background      = s.additionalInfo.trim(),
                    dailyCalorieTarget  = calories,
                    dailyProteinTarget  = protein,
                    dailyCarbsTarget    = carbs,
                    dailyFatTarget      = fat,
                    bmi             = bmi.toFloat() // Converted to Float for Room DB
                )
                _uiState.update { it.copy(isSaving = false, isSaveComplete = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, saveError = e.message ?: "Something went wrong") }
            }
        }
    }
}

// ─── Data Classes ─────────────────────────────────────────────────────────────

data class MacroResult(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)