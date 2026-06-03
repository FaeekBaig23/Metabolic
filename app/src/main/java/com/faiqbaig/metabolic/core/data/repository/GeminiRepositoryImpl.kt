package com.faiqbaig.metabolic.core.data.repository

import android.graphics.Bitmap
import com.faiqbaig.metabolic.BuildConfig
import com.faiqbaig.metabolic.core.data.local.UserProfileEntity
import com.faiqbaig.metabolic.core.data.remote.DietPlanDay
import com.faiqbaig.metabolic.core.data.remote.DietPlanMeal
import com.faiqbaig.metabolic.core.data.remote.DietPlanResponse
import com.faiqbaig.metabolic.core.data.remote.GeminiFoodAnalysis
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

class GeminiRepositoryImpl @Inject constructor() : GeminiRepository {

    // ── 1. The Meal Scanner Model ──
    private val mealAnalysisModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY.replace("\"", "").trim(),
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

    // ── 2. The Chatbot Model ──
    private val chatModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY.replace("\"", "").trim(),
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

    // ── 3. The Diet Plan Generator Model ──
    private val dietPlanModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY.replace("\"", "").trim()
    )

    override suspend fun analyzeMealFromImage(bitmap: Bitmap): Result<GeminiFoodAnalysis> = withContext(Dispatchers.IO) {
        try {
            val response = mealAnalysisModel.generateContent(
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
            val response = mealAnalysisModel.generateContent(
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
            val cleanJsonString = responseText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val jsonObject = JSONObject(cleanJsonString)

            val analysis = GeminiFoodAnalysis(
                foodName = jsonObject.optString("foodName", "Unknown Food"),
                estimatedWeightG = jsonObject.optDouble("estimatedWeightG", 0.0).coerceAtLeast(0.0),
                calories = jsonObject.optInt("calories", 0).coerceAtLeast(0),
                protein = jsonObject.optDouble("protein", 0.0).coerceAtLeast(0.0),
                carbs = jsonObject.optDouble("carbs", 0.0).coerceAtLeast(0.0),
                fat = jsonObject.optDouble("fat", 0.0).coerceAtLeast(0.0)
            )
            Result.success(analysis)
        } catch (e: Exception) {
            Result.failure(Exception("Could not read nutritional data. Please try again or take a clearer photo."))
        }
    }

    override suspend fun getChatResponse(prompt: String, userContext: String): Result<String> {
        return try {
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
            val prompt = """
                You are a professional sports nutritionist. Generate a personalized 1-day meal plan for a user with the following profile:
                - Goal: ${profile.goal}
                - Activity Level: ${profile.activityLevel}
                - Diet Type: ${profile.dietType}
                - Allergies to EXCLUDE: ${profile.allergies.ifBlank { "None" }}
                - Medical Conditions to consider: ${profile.medicalConditions.ifBlank { "None" }}
                - Daily Targets: ${profile.dailyCalorieTarget} kcal, ${profile.dailyProteinTarget}g Protein, ${profile.dailyCarbsTarget}g Carbs, ${profile.dailyFatTarget}g Fat.

                Requirements:
                1. Provide exactly 1 day of meals (dayIndex 0).
                2. FOCUS ON EVERYDAY INDIAN CUISINE: Meals MUST be realistic, standard Indian household dishes (e.g., everyday sabzis, dals, rotis, rice dishes, typical Indian breakfast items).
                3. Do NOT include fancy, exotic, or expensive imported ingredients. Only use ingredients easily available in local Indian markets and grocery stores (specifically around the Mumbai/Thane area).
                4. The day must include realistic meals categorized by mealType ("Breakfast", "Lunch", "Dinner", "Snack").
                5. The total daily macros should closely align with the user's Daily Targets.
                
                Output JSON strictly matching this schema:
                {
                  "days": [
                    {
                      "dayIndex": 0,
                      "meals": [
                        {
                          "mealType": "Breakfast",
                          "foodName": "Poha with Peanuts",
                          "estimatedWeightG": 250.0,
                          "calories": 350,
                          "protein": 8.0,
                          "carbs": 55.0,
                          "fat": 12.0
                        }
                      ]
                    }
                  ]
                }
            """.trimIndent()

            val response = dietPlanModel.generateContent(prompt)
            val rawText = response.text ?: throw Exception("Empty response from Gemini")

            val backticks = "`".repeat(3)
            val cleanString = rawText.replace(backticks + "json", "")
                .replace(backticks, "")
                .trim()

            val startIndex = cleanString.indexOf('{')
            val endIndex = cleanString.lastIndexOf('}')

            val finalJson = if (startIndex != -1 && endIndex != -1 && startIndex <= endIndex) {
                cleanString.substring(startIndex, endIndex + 1)
            } else {
                throw Exception("No valid JSON found in response.")
            }

            val jsonObject = JSONObject(finalJson)
            val daysArray = jsonObject.optJSONArray("days") ?: org.json.JSONArray()

            val parsedDays = mutableListOf<DietPlanDay>()

            for (i in 0 until daysArray.length()) {
                val dayObj = daysArray.getJSONObject(i)
                val dayIndex = dayObj.optInt("dayIndex", i)

                val mealsArray = dayObj.optJSONArray("meals") ?: org.json.JSONArray()
                val parsedMeals = mutableListOf<DietPlanMeal>()

                for (j in 0 until mealsArray.length()) {
                    val mealObj = mealsArray.getJSONObject(j)
                    parsedMeals.add(
                        DietPlanMeal(
                            mealType = mealObj.optString("mealType", "Snack"),
                            foodName = mealObj.optString("foodName", "Unknown Meal"),
                            estimatedWeightG = mealObj.optDouble("estimatedWeightG", 0.0),
                            calories = mealObj.optInt("calories", 0),
                            protein = mealObj.optDouble("protein", 0.0),
                            carbs = mealObj.optDouble("carbs", 0.0),
                            fat = mealObj.optDouble("fat", 0.0)
                        )
                    )
                }

                parsedDays.add(DietPlanDay(dayIndex = dayIndex, meals = parsedMeals))
            }

            Result.success(DietPlanResponse(days = parsedDays))

        } catch (e: Exception) {
            android.util.Log.e("METABOLIC_AI_CRASH", "FAILED TO GENERATE PLAN!", e)
            Result.failure(e)
        }
    }
}