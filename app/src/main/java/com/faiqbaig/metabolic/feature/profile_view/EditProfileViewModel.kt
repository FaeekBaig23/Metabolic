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

    fun saveProfile(onSuccess: () -> Unit) {
        val current = _profileState.value ?: return
        viewModelScope.launch {
            // Unpack the entity and pass it to your repository exactly as it expects
            repository.saveProfile(
                userId = current.userId,
                name = current.name,
                gender = current.gender,
                age = current.age,
                weightKg = current.weightKg,
                heightCm = current.heightCm,
                goal = current.goal,
                activityLevel = current.activityLevel,
                activityTypes = current.activityTypes,
                dietType = current.dietType,
                allergies = current.allergies,
                medicalConditions = current.medicalConditions,
                risks = current.risks,
                background = current.background,
                dailyCalorieTarget = current.dailyCalorieTarget,
                dailyProteinTarget = current.dailyProteinTarget,
                dailyCarbsTarget = current.dailyCarbsTarget,
                dailyFatTarget = current.dailyFatTarget,
                bmi = current.bmi
            )
            onSuccess()
        }
    }
}