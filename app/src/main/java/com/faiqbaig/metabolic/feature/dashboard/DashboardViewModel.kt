package com.faiqbaig.metabolic.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.repository.UserProfileRepository
import com.faiqbaig.metabolic.core.domain.repository.MealLogRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: UserProfileRepository,
    private val auth: FirebaseAuth,
    // ── CHANGED: Injected the MealLogRepository ──
    private val mealLogRepository: MealLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    // ── NEW: Get today's date to query the database ──
    private val todayDateString: String
        get() = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    init {
        observeUserProfile()
        observeDailyTotals() // ── NEW: Start listening to meals immediately ──
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
                        state.copy(
                            userName = profile.name,
                            goal = profile.goal,
                            greeting = generateGreeting(profile.name),
                            dailyCalorieTarget = profile.dailyCalorieTarget,
                            proteinTarget = profile.dailyProteinTarget,
                            carbsTarget = profile.dailyCarbsTarget,
                            fatTarget = profile.dailyFatTarget,
                            bmi = profile.bmi.toDouble(),
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

    // ── NEW: Listen to Room database for meal updates ──
    private fun observeDailyTotals() {
        val userId = auth.currentUser?.uid ?: return

        // Because getTodaysMeals returns a Flow, this will automatically re-run
        // every single time a meal is added or deleted in the Tracker!
        mealLogRepository.getTodaysMeals(userId, todayDateString)
            .onEach { meals ->
                _uiState.update { currentState ->
                    currentState.copy(
                        // Make sure your DashboardUiState has these properties!
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

    fun onWaterToggle(index: Int) {
        val currentGlasses = _uiState.value.waterGlasses
        val newCount = if (index < currentGlasses) index else index + 1

        _uiState.update { it.copy(waterGlasses = newCount) }
    }
}