package com.faiqbaig.metabolic.core.data.repository


import android.graphics.Bitmap
import com.faiqbaig.metabolic.BuildConfig
import com.faiqbaig.metabolic.core.data.remote.GeminiFoodAnalysis
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

class GeminiRepositoryImpl @Inject constructor() : GeminiRepository {

    // We use gemini-1.5-flash as it is the fastest and supports both text and multimodal (images)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text(
                """
                You are an expert nutritionist AI. Analyze the provided food image or description.
                Estimate the nutritional values for the entire portion shown or described.
                Ensure the foodName is in Title Case. All macros must be non-negative numbers.
                
                You MUST return ONLY a valid JSON object matching this exact schema. Do not use markdown formatting like ```json.
                {
                    "foodName": "String",
                    "estimatedWeightG": Double,
                    "calories": Int,
                    "protein": Double,
                    "carbs": Double,
                    "fat": Double
                }
                """.trimIndent()
            )
        }
    )

    override suspend fun analyzeMealFromImage(bitmap: Bitmap): Result<GeminiFoodAnalysis> = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text("Analyze this meal and provide the nutritional breakdown.")
                }
            )
            parseGeminiResponse(response.text)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to analyze image: ${e.localizedMessage}"))
        }
    }

    override suspend fun analyzeMealFromText(description: String): Result<GeminiFoodAnalysis> = withContext(Dispatchers.IO) {
        try {
            val response = generativeModel.generateContent(
                content {
                    text("Analyze this meal: $description")
                }
            )
            parseGeminiResponse(response.text)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to analyze text: ${e.localizedMessage}"))
        }
    }

    private fun parseGeminiResponse(responseText: String?): Result<GeminiFoodAnalysis> {
        if (responseText.isNullOrBlank()) {
            return Result.failure(Exception("AI returned an empty response."))
        }

        return try {
            // Clean up the string just in case Gemini ignored the "no markdown" rule
            val cleanJsonString = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonObject = JSONObject(cleanJsonString)

            val analysis = GeminiFoodAnalysis(
                foodName = jsonObject.getString("foodName"),
                estimatedWeightG = jsonObject.getDouble("estimatedWeightG").coerceAtLeast(0.0),
                calories = jsonObject.getInt("calories").coerceAtLeast(0),
                protein = jsonObject.getDouble("protein").coerceAtLeast(0.0),
                carbs = jsonObject.getDouble("carbs").coerceAtLeast(0.0),
                fat = jsonObject.getDouble("fat").coerceAtLeast(0.0)
            )
            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(Exception("Could not read nutritional data. Please try again or take a clearer photo."))
        }
    }
}