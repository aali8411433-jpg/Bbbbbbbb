package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _selectedLanguage = MutableStateFlow(loadLanguage())
    val selectedLanguage: StateFlow<Language> = _selectedLanguage.asStateFlow()

    private val _apiKey = MutableStateFlow(loadApiKey())
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _modelName = MutableStateFlow(loadModelName())
    val modelName: StateFlow<String> = _modelName.asStateFlow()

    private val _customGreeting = MutableStateFlow(loadCustomGreeting())
    val customGreeting: StateFlow<String> = _customGreeting.asStateFlow()

    private val _speechRate = MutableStateFlow(loadSpeechRate())
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _speechPitch = MutableStateFlow(loadSpeechPitch())
    val speechPitch: StateFlow<Float> = _speechPitch.asStateFlow()

    fun setLanguage(language: Language) {
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        _selectedLanguage.value = language
    }

    fun setApiKey(key: String) {
        prefs.edit().putString(KEY_API_KEY, key.trim()).apply()
        _apiKey.value = key.trim()
    }

    fun setModelName(name: String) {
        prefs.edit().putString(KEY_MODEL_NAME, name.trim()).apply()
        _modelName.value = name.trim()
    }

    fun setCustomGreeting(greeting: String) {
        prefs.edit().putString(KEY_CUSTOM_GREETING, greeting.trim()).apply()
        _customGreeting.value = greeting.trim()
    }

    fun setSpeechRate(rate: Float) {
        prefs.edit().putFloat(KEY_SPEECH_RATE, rate).apply()
        _speechRate.value = rate
    }

    fun setSpeechPitch(pitch: Float) {
        prefs.edit().putFloat(KEY_SPEECH_PITCH, pitch).apply()
        _speechPitch.value = pitch
    }

    private fun loadLanguage(): Language {
        val code = prefs.getString(KEY_LANGUAGE, Language.BENGALI.code) ?: Language.BENGALI.code
        return Language.fromCode(code)
    }

    private fun loadApiKey(): String {
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }

    private fun loadModelName(): String {
        return prefs.getString(KEY_MODEL_NAME, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    private fun loadCustomGreeting(): String {
        return prefs.getString(KEY_CUSTOM_GREETING, Language.BENGALI.defaultGreeting)
            ?: Language.BENGALI.defaultGreeting
    }

    private fun loadSpeechRate(): Float {
        return prefs.getFloat(KEY_SPEECH_RATE, 1.0f)
    }

    private fun loadSpeechPitch(): Float {
        return prefs.getFloat(KEY_SPEECH_PITCH, 1.0f)
    }

    companion object {
        private const val PREFS_NAME = "jarvis_ai_preferences"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_API_KEY = "key_api_key"
        private const val KEY_MODEL_NAME = "key_model_name"
        private const val KEY_CUSTOM_GREETING = "key_custom_greeting"
        private const val KEY_SPEECH_RATE = "key_speech_rate"
        private const val KEY_SPEECH_PITCH = "key_speech_pitch"

        const val DEFAULT_MODEL = "gemini-2.0-flash"
    }
}
