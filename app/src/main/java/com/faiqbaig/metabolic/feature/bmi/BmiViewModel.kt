package com.faiqbaig.metabolic.feature.bmi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.local.WeightLogEntity
import com.faiqbaig.metabolic.core.data.repository.UserProfileRepository
import com.faiqbaig.metabolic.core.data.repository.WeightLogRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class BmiViewModel @Inject constructor(
    private val weightLogRepository: WeightLogRepository,
    private val userProfileRepository: UserProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val currentUserId = auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(BmiUiState())
    val uiState: StateFlow<BmiUiState> = _uiState.asStateFlow()

    private var userHeightCm: Double = 0.0

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Fetch User Profile for Height
        viewModelScope.launch {
            // FIX: Removed currentUserId argument to match your repository signature
            userProfileRepository.getProfile().collectLatest { profile ->
                profile?.let {
                    // FIX: Converted Float to Double to match BmiUiState
                    userHeightCm = it.heightCm.toDouble()

                    // If no logs exist yet, compute initial BMI from profile weight
                    if (_uiState.value.latestWeightKg == 0.0) {
                        updateBmiCalculation(it.weightKg.toDouble())
                    }
                }
            }
        }

        // Fetch Weight History
        viewModelScope.launch {
            weightLogRepository.getWeightHistory(currentUserId).collectLatest { logs ->
                val latestLog = logs.firstOrNull()

                if (latestLog != null) {
                    _uiState.update { it.copy(
                        latestWeightKg = latestLog.weightKg,
                        weightInputField = latestLog.weightKg.toString() // Pre-populate input
                    )}
                    updateBmiCalculation(latestLog.weightKg)
                }

                // Refresh the list and chart whenever the DB updates
                filterLogs(logs, _uiState.value.historyFilter)
            }
        }
    }

    fun onWeightInputChange(value: String) {
        _uiState.update { it.copy(weightInputField = value) }
    }

    fun onNoteChange(value: String) {
        _uiState.update { it.copy(noteField = value) }
    }

    fun onFilterChange(filter: HistoryFilter) {
        _uiState.update { it.copy(historyFilter = filter) }

        // Re-trigger filtering with the new filter state
        viewModelScope.launch {
            weightLogRepository.getWeightHistory(currentUserId).firstOrNull()?.let { logs ->
                filterLogs(logs, filter)
            }
        }
    }

    fun onLogWeight() {
        val weightStr = _uiState.value.weightInputField
        val note = _uiState.value.noteField.takeIf { it.isNotBlank() }
        val weight = weightStr.toDoubleOrNull()

        if (weight == null || weight <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid weight.") }
            return
        }

        if (userHeightCm <= 0) {
            _uiState.update { it.copy(error = "Height data missing from profile. Please complete setup.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                weightLogRepository.logWeight(currentUserId, weight, userHeightCm, note)
                // Clear the note on success, but keep the weight populated for next time
                _uiState.update { it.copy(noteField = "", isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "Failed to log weight: ${e.message}") }
            }
        }
    }

    fun onDeleteLog(entry: WeightLogEntity) {
        viewModelScope.launch {
            weightLogRepository.deleteLog(entry)
        }
    }

    private fun updateBmiCalculation(weightKg: Double) {
        if (userHeightCm > 0) {
            val heightM = userHeightCm / 100.0
            val bmi = weightKg / (heightM * heightM)
            val category = getBmiCategory(bmi)
            _uiState.update { it.copy(currentBmi = bmi, bmiCategory = category) }
        }
    }

    private fun getBmiCategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
    }

    private fun filterLogs(allLogs: List<WeightLogEntity>, filter: HistoryFilter) {
        val thresholdDate = when (filter) {
            HistoryFilter.WEEK -> LocalDate.now().minusDays(7)
            HistoryFilter.MONTH -> LocalDate.now().minusDays(30)
            HistoryFilter.ALL -> LocalDate.MIN
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val filteredLogs = allLogs.filter { log ->
            try {
                val logDate = LocalDate.parse(log.date, formatter)
                !logDate.isBefore(thresholdDate)
            } catch (_: Exception) { // FIX: Replaced 'e' with '_' to clear the warning
                true // Fallback to keep data if parse fails
            }
        }

        // Map logs to simple X,Y pairs for the Canvas chart (Date String -> Weight Double)
        val chartPoints = filteredLogs.sortedBy { it.timestamp }.map { log ->
            val dateStr = try {
                LocalDate.parse(log.date, formatter).format(DateTimeFormatter.ofPattern("MMM dd"))
            } catch (_: Exception) { // FIX: Replaced 'e' with '_' to clear the warning
                log.date
            }
            Pair(dateStr, log.weightKg)
        }

        _uiState.update { it.copy(
            weightLogs = filteredLogs,
            chartDataPoints = chartPoints
        )}
    }
}