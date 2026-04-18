package com.faiqbaig.metabolic.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: UserProfileRepository,
    private val auth: FirebaseAuth
    // TODO (Step 6): Inject MealLogRepository here
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeUserProfile()
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
                // Handle potential database read errors silently for now, just stop loading
                _uiState.update { it.copy(isLoading = false) }
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

        // Extract first name for a friendlier greeting if a full name was provided
        val firstName = name.substringBefore(" ")
        return "$timeOfDay, $firstName 👋"
    }

    fun onWaterToggle(index: Int) {
        // Stub: In the future, this will write to PreferencesManager/DataStore
        // For now, we'll just implement the visual toggle logic so the UI reacts
        val currentGlasses = _uiState.value.waterGlasses
        val newCount = if (index < currentGlasses) index else index + 1

        _uiState.update { it.copy(waterGlasses = newCount) }
    }
}