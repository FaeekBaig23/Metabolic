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
import com.faiqbaig.metabolic.core.data.local.UserProfileEntity
import com.faiqbaig.metabolic.core.data.remote.DietPlanResponse
import com.squareup.moshi.Moshi

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

    // ── The Chatbot Model ──
    private val chatModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text(
                "You are a highly knowledgeable, encouraging, and friendly fitness and nutrition AI assistant for an app called Metabolic. " +
                        "Your job is to help users with workout routines, exercise advice, muscle targeting, diet plans, and general health tips. " +
                        "Keep your answers concise and easy to read on a mobile screen. " +
                        "CRITICAL RULES: " +
                        "1. Never start your responses with greetings like 'Hey there!', 'Hello', or 'Hi'. Jump straight into answering the user's question. " +
                        "2. NEVER use markdown bolding or asterisks (do not use **text**). " +
                        "3. When creating lists or bullet points, ONLY use a standard hyphen (-) followed by a space. NEVER use asterisks (*) for lists."
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

    override suspend fun generateDietPlan(profile: UserProfileEntity): Result<DietPlanResponse> {
        return try {
            // Construct a strict prompt using the user's profile data
            val prompt = """
            You are a professional sports nutritionist. Generate a personalized 7-day meal plan for a user with the following profile:
            - Goal: ${profile.goal}
            - Activity Level: ${profile.activityLevel}
            - Diet Type: ${profile.dietType}
            - Allergies to EXCLUDE: ${profile.allergies.ifBlank { "None" }}
            - Medical Conditions to consider: ${profile.medicalConditions.ifBlank { "None" }}
            - Daily Targets: ${profile.dailyCalorieTarget} kcal, ${profile.dailyProteinTarget}g Protein, ${profile.dailyCarbsTarget}g Carbs, ${profile.dailyFatTarget}g Fat.

            Requirements:
            1. Provide exactly 7 days of meals (dayIndex 0 to 6, where 0 is Monday).
            2. Each day must include realistic, culturally accessible meals categorized by mealType ("Breakfast", "Lunch", "Dinner", "Snack").
            3. Vary the meals across the 7 days (do not repeat the same meal on consecutive days).
            4. The total daily macros for each day should closely align with the user's Daily Targets.
            5. Return ONLY a valid JSON object matching this schema exactly. No markdown, no preamble, no code blocks:
            {
              "days": [
                {
                  "dayIndex": 0,
                  "meals": [
                    {
                      "mealType": "Breakfast",
                      "foodName": "Oatmeal with Banana",
                      "estimatedWeightG": 300.0,
                      "calories": 350,
                      "protein": 12.0,
                      "carbs": 60.0,
                      "fat": 6.0
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

            // Configure the model to return JSON
            val model = generativeModel // Assuming you have this initialized
            val response = model.generateContent(prompt)

            val jsonText = response.text?.trim()?.removePrefix("```json")?.removeSuffix("```")?.trim()
                ?: throw Exception("Empty response from Gemini")

            // Parse JSON using Moshi (or kotlinx.serialization if you are using that instead)
            val moshi = Moshi.Builder().build()
            val adapter = moshi.adapter(DietPlanResponse::class.java)
            val planResponse = adapter.fromJson(jsonText)
                ?: throw Exception("Failed to parse Gemini JSON response")

            Result.success(planResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}