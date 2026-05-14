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
        modelName = "gemini-2.5-flash",
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

    // ── NEW: A separate model specifically for the Chatbot ──
    private val chatModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text(
                "You are a highly knowledgeable, encouraging, and friendly fitness and nutrition AI assistant for an app called Metabolic. " +
                        "Your job is to help users with workout routines, exercise advice, muscle targeting, diet plans, and general health tips. " +
                        "Keep your answers concise, easy to read on a mobile screen, and use bullet points when listing exercises or foods. " +
                        "CRITICAL RULE: Never start your responses with greetings like 'Hey there!', 'Hello', or 'Hi'. Jump straight into answering the user's question."
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

    override suspend fun getChatResponse(prompt: String, userContext: String): Result<String> {
        return try {
            // Invisible context injection
            val enrichedPrompt = if (userContext.isNotBlank()) {
                """
                [System Note: The user has the following profile stats: $userContext. 
                Keep this in mind for personalized advice, but do NOT awkwardly mention these stats unless it is directly relevant to their question.]
                
                User's message: $prompt
                """.trimIndent()
            } else {
                prompt
            }

            val response = chatModel.generateContent(enrichedPrompt)
            Result.success(response.text ?: "I'm sorry, I couldn't process that.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}