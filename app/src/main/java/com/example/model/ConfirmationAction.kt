package com.example.model

sealed interface ConfirmationAction {
    val description: String
    val actionType: String

    data class PlaceCall(
        val phoneNumber: String,
        val contactName: String? = null
    ) : ConfirmationAction {
        override val description: String
            get() = if (contactName != null) {
                "Call $contactName ($phoneNumber)?"
            } else {
                "Call phone number $phoneNumber?"
            }
        override val actionType: String = "Voice Call"
    }

    data class SendSms(
        val phoneNumber: String,
        val messageBody: String
    ) : ConfirmationAction {
        override val description: String
            get() = "Send SMS to $phoneNumber: \"$messageBody\"?"
        override val actionType: String = "SMS Message"
    }

    data class OpenWhatsApp(
        val phoneNumber: String,
        val messageBody: String
    ) : ConfirmationAction {
        override val description: String
            get() = "Open WhatsApp to message $phoneNumber: \"$messageBody\"?"
        override val actionType: String = "WhatsApp"
    }
}
