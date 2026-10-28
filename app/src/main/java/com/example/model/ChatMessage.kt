package com.example.model

import java.util.UUID

enum class CommandCategory(val label: String) {
    GREETING("Greeting"),
    DEVICE_ACTION("Device Action"),
    APP_LAUNCH("App Launch"),
    ACCESSIBILITY("Screen Action"),
    SYSTEM_INFO("System Info"),
    CALL_MESSAGE("Call / Message"),
    AI_QUERY("Gemini AI"),
    SETTINGS("Settings"),
    SYSTEM_ERROR("Alert")
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val category: CommandCategory = if (isFromUser) CommandCategory.DEVICE_ACTION else CommandCategory.AI_QUERY
)
