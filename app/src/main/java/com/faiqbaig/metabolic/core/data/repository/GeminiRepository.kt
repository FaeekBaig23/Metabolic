package com.faiqbaig.metabolic.core.data.repository

import android.graphics.Bitmap
import com.faiqbaig.metabolic.core.data.remote.GeminiFoodAnalysis
import com.faiqbaig.metabolic.core.data.local.UserProfileEntity
import com.faiqbaig.metabolic.core.data.remote.DietPlanResponse

interface GeminiRepository {
    suspend fun analyzeMealFromImage(bitmap: Bitmap): Result<GeminiFoodAnalysis>
    suspend fun analyzeMealFromText(description: String): Result<GeminiFoodAnalysis>
    suspend fun getChatResponse(prompt: String, userContext: String = ""): Result<String>
    suspend fun generateDietPlan(profile: UserProfileEntity): Result<DietPlanResponse>
}