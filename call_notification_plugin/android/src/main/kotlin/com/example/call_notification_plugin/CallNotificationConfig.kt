package com.example.call_notification_plugin

enum class CallNotificationType {
    INCOMING_RINGING,
    OUTGOING_RINGING, 
    ESTABLISHED,
    INCOMING_CONNECTING
}

enum class CallAction(val value: String) {
    ANSWER("answer"),
    DECLINE("decline"),
    ANSWER_VIDEO("answerVideo"),
    ANSWER_AUDIO("answerAudio"),
    HANGUP("hangup")
}

data class CallNotificationConfig(
    val callerId: String,
    val callerName: String,
    val callerAvatar: String? = null,
    val isVideoCall: Boolean,
    val type: CallNotificationType,
    val ringtoneUri: String? = null,
    val enableVibration: Boolean = true,
    val customColor: Int? = null
)