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

    private val todayDateString: String
        get() = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    init {
        observeUserProfile()
        observeDailyTotals()
        observeLatestWeightLog()
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
                        // Initial values: use profile data unless overridden by a newer weight log
                        val currentBmi = if (state.bmi == 0.0) profile.bmi.toDouble() else state.bmi
                        val currentWeight = if (state.weightKg == 0.0) profile.weightKg.toDouble() else state.weightKg
                        val currentHeight = if (state.heightCm == 0.0) profile.heightCm.toDouble() else state.heightCm

                        state.copy(
                            userName = profile.name,
                            goal = profile.goal,
                            greeting = generateGreeting(profile.name),
                            dailyCalorieTarget = profile.dailyCalorieTarget,
                            proteinTarget = profile.dailyProteinTarget,
                            carbsTarget = profile.dailyCarbsTarget,
                            fatTarget = profile.dailyFatTarget,
                            bmi = currentBmi,
                            weightKg = currentWeight, // ── UPDATED ──
                            heightCm = currentHeight, // ── UPDATED ──
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

    private fun observeLatestWeightLog() {
        val userId = auth.currentUser?.uid ?: return

        weightLogRepository.getLatestLog(userId)
            .onEach { latestLog ->
                if (latestLog != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            bmi = latestLog.bmi,
                            weightKg = latestLog.weightKg // ── UPDATED ──
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

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
        return "$timeOfDay, $firstName"
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
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