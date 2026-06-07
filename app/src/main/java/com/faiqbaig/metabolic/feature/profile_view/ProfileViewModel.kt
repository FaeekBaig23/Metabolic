package com.faiqbaig.metabolic.feature.profile_view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.repository.UserProfileRepository
import com.faiqbaig.metabolic.core.data.repository.WeightLogRepository
import com.faiqbaig.metabolic.core.utils.PreferencesManager
import com.faiqbaig.metabolic.core.utils.MetabolicNotificationManager // ── ADDED IMPORT ──
import com.faiqbaig.metabolic.core.utils.ReminderManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext // ── ADDED IMPORT ──
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: UserProfileRepository,
    private val preferencesManager: PreferencesManager,
    private val auth: FirebaseAuth,
    private val weightLogRepository: WeightLogRepository,
    @ApplicationContext private val context: Context // ── INJECTED CONTEXT ──
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // Initialize our new Notification Manager
    private val notificationManager = MetabolicNotificationManager(context)

    init {
        observeProfileData()
        observeSettingsData()
        observeProfileImage()
        observeLatestWeight()
    }

    private fun observeProfileData() {
        viewModelScope.launch {
            profileRepository.getProfile().collect { profile ->
                _uiState.update {
                    it.copy(
                        userProfile = profile,
                        latestWeightKg = it.latestWeightKg ?: profile?.weightKg,
                        latestBmi = it.latestBmi ?: profile?.bmi,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun observeSettingsData() {
        viewModelScope.launch { preferencesManager.dailyWaterTarget.collect { target -> _uiState.update { it.copy(dailyWaterTargetMl = target) } } }
        viewModelScope.launch { preferencesManager.weightUnit.collect { unit -> _uiState.update { it.copy(weightUnit = unit) } } }
        viewModelScope.launch { preferencesManager.heightUnit.collect { unit -> _uiState.update { it.copy(heightUnit = unit) } } }
        viewModelScope.launch { preferencesManager.mealReminders.collect { enabled -> _uiState.update { it.copy(mealRemindersEnabled = enabled) } } }
        viewModelScope.launch { preferencesManager.hydrationReminders.collect { enabled -> _uiState.update { it.copy(hydrationRemindersEnabled = enabled) } } }
        viewModelScope.launch { preferencesManager.weightReminders.collect { enabled -> _uiState.update { it.copy(weightRemindersEnabled = enabled) } } }
    }

    private fun observeProfileImage() {
        viewModelScope.launch {
            preferencesManager.profileImageUri.collect { uri ->
                _uiState.update { it.copy(profileImageUri = uri) }
            }
        }
    }

    fun updateProfileImage(uri: String) {
        viewModelScope.launch { preferencesManager.setProfileImageUri(uri) }
    }

    fun updateWaterTarget(ml: Int) = viewModelScope.launch { preferencesManager.setDailyWaterTarget(ml) }
    fun updateWeightUnit(unit: String) = viewModelScope.launch { preferencesManager.setWeightUnit(unit) }
    fun updateHeightUnit(unit: String) = viewModelScope.launch { preferencesManager.setHeightUnit(unit) }

    // ── UPDATED: Toggle Actions now trigger instant test notifications ──
    fun toggleMealReminders(enabled: Boolean) = viewModelScope.launch {
        preferencesManager.setMealReminders(enabled)
        if (enabled) {
            // Schedule for 6:00 PM (18:00)
            ReminderManager.scheduleReminder(context, "MEAL", 18, 0)
        } else {
            // Cancel if the user turns the switch off
            ReminderManager.cancelReminder(context, "MEAL")
        }
    }

    fun toggleHydrationReminders(enabled: Boolean) = viewModelScope.launch {
        preferencesManager.setHydrationReminders(enabled)
        if (enabled) {
            // Schedule for 12:00 PM (12:00)
            ReminderManager.scheduleReminder(context, "HYDRATION", 12, 0)
        } else {
            // Cancel if the user turns the switch off
            ReminderManager.cancelReminder(context, "HYDRATION")
        }
    }

    fun toggleWeightReminders(enabled: Boolean) = viewModelScope.launch {
        preferencesManager.setWeightReminders(enabled)
        // Optional: Add a test notification for weight here too if you want!
    }

    fun sendPasswordResetEmail() {
        val email = auth.currentUser?.email ?: return
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _uiState.update { it.copy(isPasswordResetSent = true, error = null) }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(error = e.message ?: "Failed to send reset email") }
            }
    }

    fun dismissPasswordResetMessage() {
        _uiState.update { it.copy(isPasswordResetSent = false) }
    }

    fun showDeleteConfirmation(show: Boolean) {
        _uiState.update { it.copy(showDeleteConfirmation = show) }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e ->
                    _uiState.update { it.copy(error = e.localizedMessage ?: "Failed to delete account. Try logging out and back in first.", showDeleteConfirmation = false) }
                }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun observeLatestWeight() {
        val userId = auth.currentUser?.uid ?: "CURRENT_USER_ID"

        viewModelScope.launch {
            weightLogRepository.getLatestLog(userId).collect { latestLog ->
                if (latestLog != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            latestWeightKg = latestLog.weightKg.toFloat(),
                            latestBmi = latestLog.bmi.toFloat()
                        )
                    }
                }
            }
        }
    }
}