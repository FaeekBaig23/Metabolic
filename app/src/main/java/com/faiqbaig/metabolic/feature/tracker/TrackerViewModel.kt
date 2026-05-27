package com.faiqbaig.metabolic.feature.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.local.MealLogEntity
import com.faiqbaig.metabolic.core.domain.repository.MealLogRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val repository: MealLogRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(TrackerUiState())
    val state: StateFlow<TrackerUiState> = _state.asStateFlow()

    // Get the current user's ID, default to a fallback if logged out during testing
    private val userId: String
        get() = auth.currentUser?.uid ?: "test_user_id"

    private val todayDateString: String
        get() = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    init {
        observeTodaysMeals()
    }

    private fun observeTodaysMeals() {
        // Room returns a Flow, so anytime a meal is added/deleted, this automatically triggers!
        repository.getTodaysMeals(userId, todayDateString)
            .onEach { meals ->
                _state.update { it.copy(todaysMeals = meals) }
            }
            .launchIn(viewModelScope)
    }

    // ─── LOGGING MEALS ───────────────────────────────────────────────────────

    fun logMeal(
        foodName: String, calories: Int, protein: Int, carbs: Int, fat: Int,
        servingQty: Float, servingUnit: String, mealType: String
    ) {
        viewModelScope.launch {
            val entity = MealLogEntity(
                userId = userId,
                date = todayDateString,
                mealType = mealType,
                foodName = foodName,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                servingQty = servingQty,
                servingUnit = servingUnit
            )
            repository.logMeal(entity)
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            repository.deleteMealLog(mealId)
        }
    }
}