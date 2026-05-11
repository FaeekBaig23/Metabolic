package com.faiqbaig.metabolic.feature.camera

import android.graphics.Bitmap
import com.faiqbaig.metabolic.core.data.remote.GeminiFoodAnalysis

data class CameraUiState(
    val isAnalyzing: Boolean = false,
    val textInput: String = "",
    val selectedImageBitmap: Bitmap? = null,
    val analysisResult: GeminiFoodAnalysis? = null,
    val error: String? = null
)