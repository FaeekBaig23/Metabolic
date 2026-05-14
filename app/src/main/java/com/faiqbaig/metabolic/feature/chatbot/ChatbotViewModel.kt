package com.faiqbaig.metabolic.feature.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.repository.GeminiRepository
import com.faiqbaig.metabolic.core.data.repository.UserProfileRepository
import com.faiqbaig.metabolic.core.data.repository.WeightLogRepository
import com.faiqbaig.metabolic.core.domain.repository.MealLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val profileRepository: UserProfileRepository,
    private val weightLogRepository: WeightLogRepository,
    private val mealLogRepository: MealLogRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Hi! I'm your Metabolic AI assistant. Ask me for workout tips, recipe ideas, or how to hit your protein goals today!",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun onInputTextChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val userText = _inputText.value.trim()
        if (userText.isBlank()) return

        _inputText.value = ""

        val userMsg = ChatMessage(text = userText, isUser = true)
        val loadingMsgId = java.util.UUID.randomUUID().toString()
        val loadingMsg = ChatMessage(id = loadingMsgId, text = "", isUser = false, isLoading = true)

        val chatTranscript = _messages.value
            .filter { it.text.isNotBlank() && !it.isLoading }
            .joinToString(separator = "\n") { if (it.isUser) "User: ${it.text}" else "AI: ${it.text}" }

        _messages.update { currentList ->
            currentList + userMsg + loadingMsg
        }

        viewModelScope.launch {
            // 1. Fetch static profile data
            val userProfile = profileRepository.getProfileOnce()

            // Prepare dynamic variables
            var currentWeight = userProfile?.weightKg ?: 0f
            var dynamicBmi = userProfile?.bmi ?: 0f
            var totalCalsEaten = 0
            var totalProteinEaten = 0
            var totalCarbsEaten = 0
            var totalFatEaten = 0

            // 2. Fetch active dynamic data if user exists
            if (userProfile != null && userProfile.userId.isNotEmpty()) {
                val userId = userProfile.userId

                // Grab the single latest weight entry from the Flow
                val latestWeightLog = weightLogRepository.getLatestLog(userId).firstOrNull()
                if (latestWeightLog != null) {
                    currentWeight = latestWeightLog.weightKg.toFloat()
                    // Recalculate BMI with the new weight
                    if (userProfile.heightCm > 0f) {
                        val heightM = userProfile.heightCm / 100f
                        dynamicBmi = currentWeight / (heightM * heightM)
                    }
                }

                // Grab today's meals from the Flow
                // (Note: Adjust the date format string if your DB saves dates differently!)
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val todaysMeals = mealLogRepository.getTodaysMeals(userId, todayStr).firstOrNull()

                todaysMeals?.forEach { meal ->
                    // Using toInt() just in case your Entity uses Doubles for macros
                    totalCalsEaten += meal.calories.toInt()
                    totalProteinEaten += meal.protein.toInt()
                    totalCarbsEaten += meal.carbs.toInt()
                    totalFatEaten += meal.fat.toInt()
                }
            }

            val formattedBmi = String.format(Locale.US, "%.1f", dynamicBmi)

            // 3. Build the ultimate context string
            val userStats = if (userProfile != null) {
                """
                User Profile Information:
                - Name: ${userProfile.name}
                - Age/Gender: ${userProfile.age} / ${userProfile.gender}
                - Body: Current Weight is ${currentWeight}kg, Height is ${userProfile.heightCm}cm (Current Dynamic BMI: $formattedBmi)
                - Primary Goal: ${userProfile.goal}
                - Activity Level: ${userProfile.activityLevel} (${userProfile.activityTypes})
                - Diet Type: ${userProfile.dietType}
                - Medical Context: Allergies (${userProfile.allergies}), Conditions (${userProfile.medicalConditions}), Risks (${userProfile.risks})
                
                Today's Nutrition Logs:
                - Daily Target: ${userProfile.dailyCalorieTarget} kcal (${userProfile.dailyProteinTarget}g Protein, ${userProfile.dailyCarbsTarget}g Carbs, ${userProfile.dailyFatTarget}g Fat)
                - Eaten Today: $totalCalsEaten kcal ($totalProteinEaten g Protein, $totalCarbsEaten g Carbs, $totalFatEaten g Fat)
                - Remaining Today: ${userProfile.dailyCalorieTarget - totalCalsEaten} kcal
                """.trimIndent()
            } else {
                "User profile not fully set up yet."
            }

            val enrichedContext = """
                $userStats
                
                Previous Conversation History:
                $chatTranscript
            """.trimIndent()

            // 4. Send to Gemini
            val result = geminiRepository.getChatResponse(userText, enrichedContext)

            _messages.update { currentList ->
                currentList.map { msg ->
                    if (msg.id == loadingMsgId) {
                        result.fold(
                            onSuccess = { text -> msg.copy(text = text, isLoading = false) },
                            onFailure = { err -> msg.copy(text = "Error: ${err.localizedMessage}", isLoading = false) }
                        )
                    } else {
                        msg
                    }
                }
            }
        }
    }
}