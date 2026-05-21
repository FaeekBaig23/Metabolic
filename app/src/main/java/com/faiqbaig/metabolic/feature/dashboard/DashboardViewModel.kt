package com.faiqbaig.metabolic.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.repository.UserProfileRepository
import com.faiqbaig.metabolic.core.domain.repository.MealLogRepository
import com.faiqbaig.metabolic.core.data.repository.WeightLogRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

import com.faiqbaig.metabolic.core.utils.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: UserProfileRepository,
    private val auth: FirebaseAuth,
    private val weightLogRepository: WeightLogRepository,
    private val mealLogRepository: MealLogRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val profileImageUri: StateFlow<String?> = preferencesManager.profileImageUri
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // ── NEW: Get today's date to query the database ──
    private val todayDateString: String
        get() = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    init {
        observeUserProfile()
        observeDailyTotals() // Listen to meals
        observeLatestWeightLog() // ── NEW: Listen to weight logs immediately ──
        observeWaterIntake()
    }

    private fun observeUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        repository.getProfile()
            .onEach { profile ->
                if (profile != null) {
                    _uiState.update { state ->
                        // Only set BMI from profile if it hasn't been overridden by a newer weight log yet
                        // (The separate observer handles updates, but this gives an initial value if no logs exist)
                        val currentBmi = if (state.bmi == 0.0) profile.bmi.toDouble() else state.bmi

                        state.copy(
                            userName = profile.name,
                            goal = profile.goal,
                            greeting = generateGreeting(profile.name),
                            dailyCalorieTarget = profile.dailyCalorieTarget,
                            proteinTarget = profile.dailyProteinTarget,
                            carbsTarget = profile.dailyCarbsTarget,
                            fatTarget = profile.dailyFatTarget,
                            bmi = currentBmi,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
            .catch {
                _uiState.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    // ── NEW: Listen to Room database for the latest weight/BMI update ──
    private fun observeLatestWeightLog() {
        val userId = auth.currentUser?.uid ?: return

        weightLogRepository.getLatestLog(userId)
            .onEach { latestLog ->
                if (latestLog != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            bmi = latestLog.bmi
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    // Listen to Room database for meal updates
    private fun observeDailyTotals() {
        val userId = auth.currentUser?.uid ?: return

        mealLogRepository.getTodaysMeals(userId, todayDateString)
            .onEach { meals ->
                _uiState.update { currentState ->
                    currentState.copy(
                        todaysMeals = meals,
                        totalCalories = meals.sumOf { it.calories },
                        totalProtein = meals.sumOf { it.protein },
                        totalCarbs = meals.sumOf { it.carbs },
                        totalFat = meals.sumOf { it.fat }
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun generateGreeting(name: String): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val timeOfDay = when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }

        val firstName = name.substringBefore(" ")
        return "$timeOfDay, $firstName 👋"
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            // Write directly to DataStore.
            // The observeWaterIntake() flow will automatically catch the change and update the UI!
            preferencesManager.addWater(amountMl)
        }
    }

    private fun observeWaterIntake() {
        preferencesManager.waterConsumedFlow
            .onEach { waterAmount ->
                _uiState.update { it.copy(waterConsumedMl = waterAmount) }
            }
            .launchIn(viewModelScope)
    }
}