package com.faiqbaig.metabolic.feature.camera

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.repository.GeminiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun onTextInputChange(text: String) {
        _uiState.update { it.copy(textInput = text) }
    }

    fun onImageSelected(bitmap: Bitmap) {
        _uiState.update { it.copy(selectedImageBitmap = bitmap, error = null) }
    }

    fun clearImage() {
        _uiState.update { it.copy(selectedImageBitmap = null, error = null) }
    }

    fun clearResult() {
        _uiState.update { it.copy(analysisResult = null, textInput = "", selectedImageBitmap = null, error = null) }
    }

    fun analyzeMealFromText() {
        val description = _uiState.value.textInput
        if (description.isBlank()) {
            _uiState.update { it.copy(error = "Please describe what you ate.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, error = null, analysisResult = null) }

            val result = geminiRepository.analyzeMealFromText(description)

            result.fold(
                onSuccess = { analysis ->
                    _uiState.update { it.copy(isAnalyzing = false, analysisResult = analysis) }
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(isAnalyzing = false, error = exception.localizedMessage) }
                }
            )
        }
    }

    fun analyzeMealFromImage() {
        val bitmap = _uiState.value.selectedImageBitmap
        if (bitmap == null) {
            _uiState.update { it.copy(error = "Please select or take a photo first.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, error = null, analysisResult = null) }

            val result = geminiRepository.analyzeMealFromImage(bitmap)

            result.fold(
                onSuccess = { analysis ->
                    _uiState.update { it.copy(isAnalyzing = false, analysisResult = analysis) }
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(isAnalyzing = false, error = exception.localizedMessage) }
                }
            )
        }
    }

    fun clearError() {
        // Depending on how your state is set up, it will look like one of these:

        // If you are using a MutableStateFlow with .update {}:
        _uiState.value = _uiState.value.copy(error = null)

        // OR if your state uses update():
        // _uiState.update { it.copy(error = null) }
    }
}