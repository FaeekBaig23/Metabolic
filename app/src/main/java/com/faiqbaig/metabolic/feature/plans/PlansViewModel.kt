package com.faiqbaig.metabolic.feature.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.local.DietPlanMealEntity
import com.faiqbaig.metabolic.core.data.repository.DietPlanRepository
import com.faiqbaig.metabolic.core.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val dietPlanRepository: DietPlanRepository,
    private val profileRepository: UserProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlansUiState())
    val uiState: StateFlow<PlansUiState> = _uiState.asStateFlow()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    init {
        observeActivePlan()
    }

    private fun observeActivePlan() {
        val userId = currentUserId ?: return

        viewModelScope.launch {
            dietPlanRepository.getActivePlan(userId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to load plan") }
                }
                .collect { dietPlanWithMeals ->
                    if (dietPlanWithMeals != null) {
                        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
                        val date = Instant.ofEpochMilli(dietPlanWithMeals.plan.generatedAt)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()

                        _uiState.update { currentState ->
                            currentState.copy(
                                hasPlan = true,
                                plan = dietPlanWithMeals,
                                generatedOnLabel = "Generated on ${date.format(formatter)}",
                                selectedDayMeals = dietPlanWithMeals.mealsByDay[currentState.selectedDayIndex] ?: emptyList()
                            )
                        }
                    } else {
                        _uiState.update { it.copy(hasPlan = false, plan = null) }
                    }
                }
        }
    }

    fun onGeneratePlan() {
        val userId = currentUserId ?: return
        _uiState.update { it.copy(isGenerating = true, error = null) }

        viewModelScope.launch {
            try {
                val profile = profileRepository.getProfileOnce()
                if (profile != null) {
                    dietPlanRepository.generateAndSavePlan(userId, profile)
                        .onFailure { e ->
                            _uiState.update { it.copy(error = e.message ?: "Failed to generate plan") }
                        }
                } else {
                    _uiState.update { it.copy(error = "User profile not found. Cannot generate plan.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "An unexpected error occurred") }
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    fun onRegenerateClick() {
        _uiState.update { it.copy(isRegenerateDialogVisible = true) }
    }

    fun onRegenerateConfirmed() {
        val userId = currentUserId ?: return
        _uiState.update { it.copy(isRegenerateDialogVisible = false, isGenerating = true, error = null) }

        viewModelScope.launch {
            try {
                val profile = profileRepository.getProfileOnce()
                if (profile != null) {
                    dietPlanRepository.regeneratePlan(userId, profile)
                        .onFailure { e ->
                            _uiState.update { it.copy(error = e.message ?: "Failed to regenerate plan") }
                        }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "An unexpected error occurred") }
            } finally {
                _uiState.update { it.copy(isGenerating = false) }
            }
        }
    }

    fun onRegenerateDismissed() {
        _uiState.update { it.copy(isRegenerateDialogVisible = false) }
    }

    // ─── NEW DELETE LOGIC ───
    fun onDeleteClick() {
        _uiState.update { it.copy(isDeleteDialogVisible = true) }
    }

    fun onDeleteConfirmed() {
        val userId = currentUserId ?: return
        _uiState.update { it.copy(isDeleteDialogVisible = false) }

        viewModelScope.launch {
            try {
                // IMPORTANT: Ensure this function exists in your DietPlanRepository!
                dietPlanRepository.deleteActivePlan(userId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete plan") }
            }
        }
    }

    fun onDeleteDismissed() {
        _uiState.update { it.copy(isDeleteDialogVisible = false) }
    }

    fun onDaySelected(dayIndex: Int) {
        _uiState.update { currentState ->
            val mealsForDay = currentState.plan?.mealsByDay?.get(dayIndex) ?: emptyList()
            currentState.copy(
                selectedDayIndex = dayIndex,
                selectedDayMeals = mealsForDay
            )
        }
    }

    fun onLogMeal(meal: DietPlanMealEntity) {
        viewModelScope.launch {
            val todayDateString = LocalDate.now().toString()
            dietPlanRepository.logMealFromPlan(meal, todayDateString)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to log meal") }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}