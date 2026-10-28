package com.example.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.accessibility.JarvisAccessibilityService
import com.example.ai.GeminiApiClient
import com.example.data.PreferencesManager
import com.example.model.AssistantState
import com.example.model.ChatMessage
import com.example.model.CommandCategory
import com.example.model.ConfirmationAction
import com.example.model.Language
import com.example.router.CommandResult
import com.example.router.CommandRouter
import com.example.voice.VoiceSpeechManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiEvent {
    data class LaunchIntent(val intent: Intent) : UiEvent
    data class ShowToast(val message: String) : UiEvent
}

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    val prefsManager = PreferencesManager(context)
    private val commandRouter = CommandRouter(context)
    private val geminiApiClient = GeminiApiClient()
    val voiceManager = VoiceSpeechManager(context, viewModelScope)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _assistantState = MutableStateFlow<AssistantState>(AssistantState.Idle)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()

    private val _pendingConfirmation = MutableStateFlow<ConfirmationAction?>(null)
    val pendingConfirmation: StateFlow<ConfirmationAction?> = _pendingConfirmation.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _showAccessibilityDialog = MutableStateFlow(false)
    val showAccessibilityDialog: StateFlow<Boolean> = _showAccessibilityDialog.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent> = _uiEvents.asSharedFlow()

    val selectedLanguage = prefsManager.selectedLanguage
    val isAccessibilityConnected = JarvisAccessibilityService.isServiceActive

    init {
        // Observe voice manager status
        viewModelScope.launch {
            combine(voiceManager.isListening, voiceManager.isSpeaking, voiceManager.rmsVolume) { listening, speaking, rms ->
                Triple(listening, speaking, rms)
            }.collect { (listening, speaking, rms) ->
                val current = _assistantState.value
                when {
                    listening -> _assistantState.value = AssistantState.Listening(rms)
                    speaking -> {
                        if (current !is AssistantState.Speaking) {
                            _assistantState.value = AssistantState.Speaking()
                        }
                    }
                    current !is AssistantState.Processing && current !is AssistantState.Error -> {
                        _assistantState.value = AssistantState.Idle
                    }
                }
            }
        }

        // Set speech callbacks
        voiceManager.onSpeechResult = { recognizedText ->
            processVoiceInput(recognizedText)
        }

        voiceManager.onSpeechError = { errorMsg ->
            _assistantState.value = AssistantState.Error(errorMsg)
            addMessage(ChatMessage(text = errorMsg, isFromUser = false, category = CommandCategory.SYSTEM_ERROR))
        }

        // Add initial welcoming message
        val lang = prefsManager.selectedLanguage.value
        addMessage(
            ChatMessage(
                text = "${lang.welcomeMessage}\nGreeting: \"${prefsManager.customGreeting.value}\"",
                isFromUser = false,
                category = CommandCategory.GREETING
            )
        )
    }

    fun startListening() {
        val lang = prefsManager.selectedLanguage.value
        voiceManager.applyTtsLanguage(lang, prefsManager.speechRate.value, prefsManager.speechPitch.value)
        _assistantState.value = AssistantState.Listening(0f)
        voiceManager.startListening(lang)
    }

    fun stopListening() {
        voiceManager.stopListening()
        voiceManager.stopSpeaking()
        _assistantState.value = AssistantState.Idle
    }

    fun processVoiceInput(text: String) {
        if (text.isBlank()) return

        // Add user speech to transcript
        addMessage(ChatMessage(text = text, isFromUser = true, category = CommandCategory.DEVICE_ACTION))

        val lang = prefsManager.selectedLanguage.value
        val greeting = prefsManager.customGreeting.value

        when (val result = commandRouter.route(text, lang, greeting)) {
            is CommandResult.ImmediateResponse -> {
                _assistantState.value = AssistantState.Speaking(result.spokenText)
                addMessage(
                    ChatMessage(
                        text = result.displayText,
                        isFromUser = false,
                        category = result.category
                    )
                )
                voiceManager.speak(result.spokenText)
            }

            is CommandResult.LaunchIntent -> {
                _assistantState.value = AssistantState.Speaking(result.spokenText)
                addMessage(
                    ChatMessage(
                        text = result.displayText,
                        isFromUser = false,
                        category = result.category
                    )
                )
                voiceManager.speak(result.spokenText)
                viewModelScope.launch {
                    _uiEvents.emit(UiEvent.LaunchIntent(result.intent))
                }
            }

            is CommandResult.RequireConfirmation -> {
                _pendingConfirmation.value = result.action
                _assistantState.value = AssistantState.Speaking(result.spokenText)
                addMessage(
                    ChatMessage(
                        text = "${result.displayText}\n(Awaiting confirmation)",
                        isFromUser = false,
                        category = CommandCategory.CALL_MESSAGE
                    )
                )
                voiceManager.speak(result.spokenText)
            }

            is CommandResult.RouteToAi -> {
                queryGeminiAi(result.query, lang)
            }
        }
    }

    private fun queryGeminiAi(query: String, lang: Language) {
        _assistantState.value = AssistantState.Processing(query)

        viewModelScope.launch {
            val responseResult = geminiApiClient.generateResponse(
                userPrompt = query,
                language = lang,
                customApiKey = prefsManager.apiKey.value,
                modelName = prefsManager.modelName.value
            )

            responseResult.fold(
                onSuccess = { replyText ->
                    _assistantState.value = AssistantState.Speaking(replyText)
                    addMessage(
                        ChatMessage(
                            text = replyText,
                            isFromUser = false,
                            category = CommandCategory.AI_QUERY
                        )
                    )
                    voiceManager.speak(replyText)
                },
                onFailure = { error ->
                    val errorDesc = error.message ?: "Failed to generate AI response."
                    _assistantState.value = AssistantState.Error(errorDesc)
                    addMessage(
                        ChatMessage(
                            text = errorDesc,
                            isFromUser = false,
                            category = CommandCategory.SYSTEM_ERROR
                        )
                    )
                    voiceManager.speak(errorDesc)
                }
            )
        }
    }

    fun confirmAction() {
        val action = _pendingConfirmation.value ?: return
        _pendingConfirmation.value = null

        when (action) {
            is ConfirmationAction.PlaceCall -> {
                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${action.phoneNumber}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                viewModelScope.launch {
                    _uiEvents.emit(UiEvent.LaunchIntent(dialIntent))
                }
                val msg = "Opening dialer for ${action.phoneNumber}."
                voiceManager.speak(msg)
                addMessage(ChatMessage(text = msg, isFromUser = false, category = CommandCategory.CALL_MESSAGE))
            }

            is ConfirmationAction.SendSms -> {
                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("smsto:${action.phoneNumber}")
                    putExtra("sms_body", action.messageBody)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                viewModelScope.launch {
                    _uiEvents.emit(UiEvent.LaunchIntent(smsIntent))
                }
                val msg = "Opened SMS composer for ${action.phoneNumber}."
                voiceManager.speak(msg)
                addMessage(ChatMessage(text = msg, isFromUser = false, category = CommandCategory.CALL_MESSAGE))
            }

            is ConfirmationAction.OpenWhatsApp -> {
                val waUri = if (action.phoneNumber.isNotBlank()) {
                    Uri.parse("https://api.whatsapp.com/send?phone=${action.phoneNumber}&text=${Uri.encode(action.messageBody)}")
                } else {
                    Uri.parse("whatsapp://send?text=${Uri.encode(action.messageBody)}")
                }
                val waIntent = Intent(Intent.ACTION_VIEW, waUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                viewModelScope.launch {
                    _uiEvents.emit(UiEvent.LaunchIntent(waIntent))
                }
                val msg = "Opening WhatsApp."
                voiceManager.speak(msg)
                addMessage(ChatMessage(text = msg, isFromUser = false, category = CommandCategory.CALL_MESSAGE))
            }
        }
    }

    fun cancelAction() {
        _pendingConfirmation.value = null
        val msg = "Action cancelled."
        voiceManager.speak(msg)
        addMessage(ChatMessage(text = msg, isFromUser = false, category = CommandCategory.DEVICE_ACTION))
    }

    fun speakMessage(text: String) {
        _assistantState.value = AssistantState.Speaking(text)
        voiceManager.speak(text)
    }

    fun clearMessages() {
        _messages.value = emptyList()
        val lang = prefsManager.selectedLanguage.value
        addMessage(
            ChatMessage(
                text = "History cleared. JARVIS is ready.",
                isFromUser = false,
                category = CommandCategory.GREETING
            )
        )
    }

    fun setLanguage(language: Language) {
        prefsManager.setLanguage(language)
        voiceManager.applyTtsLanguage(language, prefsManager.speechRate.value, prefsManager.speechPitch.value)
        val notice = "Language switched to ${language.displayName}."
        addMessage(ChatMessage(text = notice, isFromUser = false, category = CommandCategory.GREETING))
        voiceManager.speak(language.defaultGreeting)
    }

    fun openSettingsDialog() {
        _showSettingsDialog.value = true
    }

    fun closeSettingsDialog() {
        _showSettingsDialog.value = false
    }

    fun openAccessibilityDialog() {
        _showAccessibilityDialog.value = true
    }

    fun closeAccessibilityDialog() {
        _showAccessibilityDialog.value = false
    }

    fun saveSettings(
        apiKey: String,
        model: String,
        language: Language,
        greeting: String,
        rate: Float,
        pitch: Float
    ) {
        prefsManager.setApiKey(apiKey)
        prefsManager.setModelName(model)
        prefsManager.setLanguage(language)
        prefsManager.setCustomGreeting(greeting)
        prefsManager.setSpeechRate(rate)
        prefsManager.setSpeechPitch(pitch)

        voiceManager.applyTtsLanguage(language, rate, pitch)
        _showSettingsDialog.value = false

        addMessage(
            ChatMessage(
                text = "Configuration updated successfully.",
                isFromUser = false,
                category = CommandCategory.SETTINGS
            )
        )
    }

    private fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
