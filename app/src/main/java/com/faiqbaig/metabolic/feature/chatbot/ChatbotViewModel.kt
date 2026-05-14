package com.faiqbaig.metabolic.feature.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.faiqbaig.metabolic.core.data.repository.GeminiRepository
import com.faiqbaig.metabolic.core.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository,
    private val profileRepository: UserProfileRepository // ── INJECTED ──
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

        // 1. Add User Message
        val userMsg = ChatMessage(text = userText, isUser = true)

        // 2. Add temporary "Loading" Message
        val loadingMsgId = java.util.UUID.randomUUID().toString()
        val loadingMsg = ChatMessage(id = loadingMsgId, text = "", isUser = false, isLoading = true)

        // 3. Capture the CURRENT chat history (Memory)
        val chatTranscript = _messages.value
            .filter { it.text.isNotBlank() && !it.isLoading }
            .joinToString(separator = "\n") { if (it.isUser) "User: ${it.text}" else "AI: ${it.text}" }

        _messages.update { currentList ->
            currentList + userMsg + loadingMsg
        }

        // 4. Fetch response from Gemini
        viewModelScope.launch {

            // Fetch real user data from Room database
            val userProfile = profileRepository.getProfileOnce()

            // Format the user's biological and goal data into a context string
            val userStats = if (userProfile != null) {
                """
                User Profile Information:
                - Name: ${userProfile.name}
                - Age/Gender: ${userProfile.age} / ${userProfile.gender}
                - Body: ${userProfile.weightKg}kg, ${userProfile.heightCm}cm (BMI: ${userProfile.bmi})
                - Primary Goal: ${userProfile.goal}
                - Activity Level: ${userProfile.activityLevel} (${userProfile.activityTypes})
                - Diet Type: ${userProfile.dietType}
                - Medical Context: Allergies (${userProfile.allergies}), Conditions (${userProfile.medicalConditions}), Risks (${userProfile.risks})
                - Daily Targets: ${userProfile.dailyCalorieTarget} kcal (${userProfile.dailyProteinTarget}g Protein, ${userProfile.dailyCarbsTarget}g Carbs, ${userProfile.dailyFatTarget}g Fat)
                """.trimIndent()
            } else {
                "User profile not fully set up yet."
            }

            // Combine the Profile Stats AND the Chat History
            val enrichedContext = """
                $userStats
                
                Previous Conversation History:
                $chatTranscript
            """.trimIndent()

            // Pass the user text and the enriched context to the repository
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