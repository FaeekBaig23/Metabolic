package com.faiqbaig.metabolic.feature.chatbot

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
class ChatbotViewModel @Inject constructor(
    private val geminiRepository: GeminiRepository
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

        // 2. Add temporary "Loading" Message for the AI
        val loadingMsgId = java.util.UUID.randomUUID().toString()
        val loadingMsg = ChatMessage(id = loadingMsgId, text = "", isUser = false, isLoading = true)

        _messages.update { currentList ->
            currentList + userMsg + loadingMsg
        }

        // 3. Fetch response from Gemini
        viewModelScope.launch {
            val result = geminiRepository.getChatResponse(userText)

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