package com.faiqbaig.metabolic.feature.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.local.MealLogEntity
import com.faiqbaig.metabolic.core.domain.repository.MealLogRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var searchJob: Job? = null

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

    // ─── SEARCH LOGIC ────────────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }

        searchJob?.cancel() // Cancel the previous search if they are still typing

        if (query.isBlank()) {
            _state.update { it.copy(searchResults = emptyList(), isSearching = false, searchError = null) }
            return
        }

        // Debounce: Wait 300ms after the user stops typing before hitting the USDA API
        searchJob = viewModelScope.launch {
            delay(300L)
            _state.update { it.copy(isSearching = true, searchError = null) }

            repository.searchFoods(query).fold(
                onSuccess = { foods ->
                    _state.update { it.copy(searchResults = foods, isSearching = false) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isSearching = false,
                            searchError = error.localizedMessage ?: "Failed to fetch foods"
                        )
                    }
                }
            )
        }
    }

    fun clearSearch() {
        _state.update { it.copy(searchQuery = "", searchResults = emptyList(), searchError = null) }
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
            clearSearch() // Clear the search bar after successfully logging
        }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch {
            repository.deleteMealLog(mealId)
        }
    }
}