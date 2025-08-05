package com.example.call_notification_plugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Broadcast receiver for handling call notification actions
 * Receives actions from notification buttons and forwards them to the plugin
 */
class CallNotificationReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_ANSWER_AUDIO = "com.example.call_notification_plugin.ANSWER_AUDIO"
        const val ACTION_ANSWER_VIDEO = "com.example.call_notification_plugin.ANSWER_VIDEO"
        const val ACTION_DECLINE = "com.example.call_notification_plugin.DECLINE"
        const val ACTION_HANGUP = "com.example.call_notification_plugin.HANGUP"
        const val ACTION_SHOW_CALL_SCREEN = "com.example.call_notification_plugin.SHOW_CALL_SCREEN"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = when (intent.action) {
            ACTION_ANSWER_AUDIO -> CallAction.ANSWER_AUDIO
            ACTION_ANSWER_VIDEO -> CallAction.ANSWER_VIDEO
            ACTION_DECLINE -> CallAction.DECLINE
            ACTION_HANGUP -> CallAction.HANGUP
            ACTION_SHOW_CALL_SCREEN -> {
                // For content intent, we'll just treat it as answer
                CallAction.ANSWER
            }
            else -> return
        }

        // Get the CallNotificationManager instance and trigger the action
        CallNotificationManager.getInstance()?.handleNotificationAction(action)
    }
}