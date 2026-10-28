package com.example.model

sealed interface AssistantState {
    data object Idle : AssistantState
    data class Listening(val rmsDb: Float = 0f) : AssistantState
    data class Processing(val query: String = "") : AssistantState
    data class Speaking(val text: String = "") : AssistantState
    data class Error(val message: String) : AssistantState
}
