package com.faiqbaig.metabolic.core.data.repository

import android.graphics.Bitmap
import com.faiqbaig.metabolic.core.data.remote.GeminiFoodAnalysis

interface GeminiRepository {
    suspend fun analyzeMealFromImage(bitmap: Bitmap): Result<GeminiFoodAnalysis>
    suspend fun analyzeMealFromText(description: String): Result<GeminiFoodAnalysis>

    suspend fun getChatResponse(prompt: String): Result<String>
}