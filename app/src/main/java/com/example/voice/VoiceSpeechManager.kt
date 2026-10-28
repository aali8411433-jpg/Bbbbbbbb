package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.model.Language
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class VoiceSpeechManager(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _rmsVolume = MutableStateFlow(0f)
    val rmsVolume: StateFlow<Float> = _rmsVolume.asStateFlow()

    private val _partialText = MutableStateFlow("")
    val partialText: StateFlow<String> = _partialText.asStateFlow()

    var onSpeechResult: ((String) -> Unit)? = null
    var onSpeechError: ((String) -> Unit)? = null

    init {
        initTts(Language.BENGALI)
    }

    fun initTts(language: Language, onInitDone: (() -> Unit)? = null) {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
                applyTtsLanguage(language)
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _isSpeaking.value = false
                        Log.w(TAG, "TTS Utterance error: $errorCode")
                    }
                })
                onInitDone?.invoke()
            } else {
                isTtsInitialized = false
                Log.e(TAG, "TextToSpeech initialization failed with status $status")
            }
        }
    }

    fun applyTtsLanguage(language: Language, speechRate: Float = 1.0f, pitch: Float = 1.0f) {
        if (!isTtsInitialized || textToSpeech == null) return
        val tts = textToSpeech ?: return
        val result = tts.setLanguage(language.locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "TTS Language ${language.code} missing data or not supported. Falling back to default.")
            tts.language = java.util.Locale.US
        }
        tts.setSpeechRate(speechRate)
        tts.setPitch(pitch)
    }

    fun startListening(language: Language) {
        stopSpeaking()
        vibrateFeedback()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onSpeechError?.invoke("Speech recognition service is not available on this device.")
            return
        }

        // Must run on main thread
        scope.launch(Dispatchers.Main) {
            destroyRecognizer()

            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                    _partialText.value = ""
                }

                override fun onBeginningOfSpeech() {
                    _isListening.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    _rmsVolume.value = rmsdB.coerceIn(0f, 10f)
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _rmsVolume.value = 0f
                    val errorMessage = mapSpeechError(error, language)
                    Log.w(TAG, "SpeechRecognizer error: $error ($errorMessage)")
                    onSpeechError?.invoke(errorMessage)
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _rmsVolume.value = 0f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val topResult = matches?.firstOrNull()?.trim()
                    if (!topResult.isNullOrBlank()) {
                        _partialText.value = topResult
                        onSpeechResult?.invoke(topResult)
                    } else {
                        onSpeechError?.invoke(language.noSpeechError)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()
                    if (!text.isNullOrBlank()) {
                        _partialText.value = text
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.code)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.code)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language.code)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            try {
                recognizer.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start speech recognizer", e)
                _isListening.value = false
                onSpeechError?.invoke("Could not start speech recognition: ${e.message}")
            }
        }
    }

    fun stopListening() {
        scope.launch(Dispatchers.Main) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping recognizer", e)
            }
            _isListening.value = false
            _rmsVolume.value = 0f
        }
    }

    fun speak(text: String) {
        if (!isTtsInitialized || textToSpeech == null) {
            Log.w(TAG, "TTS not ready to speak: $text")
            return
        }
        val tts = textToSpeech ?: return
        val utteranceId = UUID.randomUUID().toString()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
    }

    fun destroy() {
        destroyRecognizer()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isTtsInitialized = false
    }

    private fun destroyRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying speech recognizer", e)
        }
        speechRecognizer = null
        _isListening.value = false
        _rmsVolume.value = 0f
    }

    private fun mapSpeechError(errorCode: Int, language: Language): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_NO_MATCH -> language.noSpeechError
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> when (language) {
                Language.BENGALI -> "সময় অতিক্রম হয়েছে। কোন শব্দ পাওয়া যায়নি।"
                Language.HINDI -> "समय समाप्त हो गया। कोई आवाज़ नहीं सुनी गई।"
                Language.ENGLISH -> "Speech timeout. No voice heard."
            }
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check your microphone."
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network error during speech recognition."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required to listen."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy. Please try again."
            else -> "Speech recognition error (code $errorCode)."
        }
    }

    private fun vibrateFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(40)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vibration failed", e)
        }
    }

    companion object {
        private const val TAG = "VoiceSpeechManager"
    }
}
