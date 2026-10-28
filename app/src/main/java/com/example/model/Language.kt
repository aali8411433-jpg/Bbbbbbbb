package com.example.model

import java.util.Locale

enum class Language(
    val code: String,
    val displayName: String,
    val defaultGreeting: String,
    val noSpeechError: String,
    val welcomeMessage: String
) {
    BENGALI(
        code = "bn-IN",
        displayName = "বাংলা",
        defaultGreeting = "হ্যাঁ আসগর বাবু, বলো।",
        noSpeechError = "আমি শুনতে পাইনি। অনুগ্রহ করে আবার বলুন।",
        welcomeMessage = "JARVIS AI প্রস্তুত। কথা বলতে মাইক্রোফোনে চাপ দিন।"
    ),
    HINDI(
        code = "hi-IN",
        displayName = "हिन्दी",
        defaultGreeting = "हाँ सर, कहिए।",
        noSpeechError = "मैं सुन नहीं पाया। कृपया फिर से बोलें।",
        welcomeMessage = "जार्विस एआई तैयार है। बोलने के लिए माइक दबाएं।"
    ),
    ENGLISH(
        code = "en-US",
        displayName = "English",
        defaultGreeting = "Yes sir, I'm listening.",
        noSpeechError = "I didn't catch that. Please speak again.",
        welcomeMessage = "JARVIS AI online. Tap microphone or say Hey Jarvis."
    );

    val locale: Locale
        get() = when (this) {
            BENGALI -> Locale("bn", "IN")
            HINDI -> Locale("hi", "IN")
            ENGLISH -> Locale.US
        }

    companion object {
        fun fromCode(code: String): Language {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: BENGALI
        }
    }
}
