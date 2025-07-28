package com.example.call_notification_plugin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat

/**
 * Builds call-style notifications inspired by Signal Android implementation
 * Manages the state of VoIP items in the Android notification bar
 */
class CallNotificationBuilder(
    private val context: Context,
    private val onActionCallback: (CallAction) -> Unit
) {
    companion object {
        const val WEBRTC_NOTIFICATION = 313388
        const val WEBRTC_NOTIFICATION_RINGING = 313389
        const val CALLS_CHANNEL = "calls_v3"
        const val CALL_STATUS_CHANNEL = "call_status_v3"
        
        /**
         * API level at which call style notifications properly pop over the screen
         * and allow a user to answer a call
         */
        const val API_LEVEL_CALL_STYLE = 29
    }

    /**
     * Creates notification channels for call notifications
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // High priority channel for incoming calls
            val callsChannel = NotificationChannel(
                CALLS_CHANNEL,
                "Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming and active call notifications"
                setShowBadge(false)
                enableLights(true)
                enableVibration(true)
            }
            
            // Low priority channel for call status
            val callStatusChannel = NotificationChannel(
                CALL_STATUS_CHANNEL,
                "Call Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Call status and connection notifications"
                setShowBadge(false)
                enableVibration(false)
            }
            
            notificationManager.createNotificationChannels(listOf(callsChannel, callStatusChannel))
        }
    }

    /**
     * Gets the notification for the current call state
     */
    fun getCallNotification(config: CallNotificationConfig): Notification {
        val notificationBuilder = NotificationCompat.Builder(context, getNotificationChannel(config.type))
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle(config.callerName)
            .setOngoing(true)
            .setAutoCancel(false)

        // Create person object for call style notification
        val person = createPersonFromConfig(config)
        notificationBuilder.addPerson(person)

        when (config.type) {
            CallNotificationType.INCOMING_RINGING -> {
                notificationBuilder
                    .setContentText(getIncomingCallContentText(config))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setFullScreenIntent(createContentIntent(), true)

                // Use call style notification for API 29+
                if (deviceVersionSupportsCallStyle()) {
                    notificationBuilder.setStyle(
                        NotificationCompat.CallStyle.forIncomingCall(
                            person,
                            createDeclineIntent(),
                            createAnswerIntent(config.isVideoCall)
                        ).setIsVideo(config.isVideoCall)
                    )
                } else {
                    // Fallback actions for older Android versions
                    notificationBuilder
                        .addAction(createNotificationAction("Decline", CallAction.DECLINE))
                        .addAction(createNotificationAction(
                            if (config.isVideoCall) "Answer Video" else "Answer",
                            if (config.isVideoCall) CallAction.ANSWER_VIDEO else CallAction.ANSWER_AUDIO
                        ))
                }
            }
            
            CallNotificationType.INCOMING_CONNECTING -> {
                notificationBuilder
                    .setContentText("Connecting...")
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setContentIntent(null)
            }
            
            CallNotificationType.OUTGOING_RINGING -> {
                notificationBuilder
                    .setContentText("Establishing call...")
                    .addAction(createNotificationAction("Cancel", CallAction.HANGUP))
            }
            
            CallNotificationType.ESTABLISHED -> {
                notificationBuilder
                    .setContentText(getOngoingCallContentText(config))
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(NotificationCompat.CATEGORY_CALL)

                if (deviceVersionSupportsCallStyle()) {
                    notificationBuilder.setStyle(
                        NotificationCompat.CallStyle.forOngoingCall(
                            person,
                            createHangupIntent()
                        ).setIsVideo(config.isVideoCall)
                    )
                } else {
                    notificationBuilder.addAction(createNotificationAction("Hang Up", CallAction.HANGUP))
                }
            }
        }

        return notificationBuilder.build()
    }

    private fun createPersonFromConfig(config: CallNotificationConfig): Person {
        val personBuilder = Person.Builder()
            .setName(config.callerName)
            .setKey(config.callerId)

        // Handle avatar if provided (base64 encoded)
        config.callerAvatar?.let { avatarBase64 ->
            try {
                val decodedBytes = Base64.decode(avatarBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                bitmap?.let {
                    personBuilder.setIcon(IconCompat.createWithBitmap(it))
                }
            } catch (e: Exception) {
                // Ignore invalid avatar data
            }
        }

        return personBuilder.build()
    }

    private fun getIncomingCallContentText(config: CallNotificationConfig): String {
        return if (config.isVideoCall) {
            "Incoming video call"
        } else {
            "Incoming call"
        }
    }

    private fun getOngoingCallContentText(config: CallNotificationConfig): String {
        return if (config.isVideoCall) {
            "Ongoing video call"
        } else {
            "Ongoing call"
        }
    }

    private fun getNotificationChannel(type: CallNotificationType): String {
        return when (type) {
            CallNotificationType.INCOMING_RINGING -> CALLS_CHANNEL
            else -> CALL_STATUS_CHANNEL
        }
    }

    fun getNotificationId(type: CallNotificationType): Int {
        return if (deviceVersionSupportsCallStyle() && type == CallNotificationType.INCOMING_RINGING) {
            WEBRTC_NOTIFICATION_RINGING
        } else {
            WEBRTC_NOTIFICATION
        }
    }

    private fun deviceVersionSupportsCallStyle(): Boolean {
        return Build.VERSION.SDK_INT >= API_LEVEL_CALL_STYLE
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_SHOW_CALL_SCREEN
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createAnswerIntent(isVideoCall: Boolean): PendingIntent {
        val intent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = if (isVideoCall) {
                CallNotificationReceiver.ACTION_ANSWER_VIDEO
            } else {
                CallNotificationReceiver.ACTION_ANSWER_AUDIO
            }
        }
        return PendingIntent.getBroadcast(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createDeclineIntent(): PendingIntent {
        val intent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_DECLINE
        }
        return PendingIntent.getBroadcast(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createHangupIntent(): PendingIntent {
        val intent = Intent(context, CallNotificationReceiver::class.java).apply {
            action = CallNotificationReceiver.ACTION_HANGUP
        }
        return PendingIntent.getBroadcast(
            context,
            3,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationAction(title: String, action: CallAction): NotificationCompat.Action {
        val intent = Intent(context, CallNotificationReceiver::class.java).apply {
            this.action = when (action) {
                CallAction.ANSWER -> CallNotificationReceiver.ACTION_ANSWER_AUDIO
                CallAction.ANSWER_AUDIO -> CallNotificationReceiver.ACTION_ANSWER_AUDIO
                CallAction.ANSWER_VIDEO -> CallNotificationReceiver.ACTION_ANSWER_VIDEO
                CallAction.DECLINE -> CallNotificationReceiver.ACTION_DECLINE
                CallAction.HANGUP -> CallNotificationReceiver.ACTION_HANGUP
            }
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            action.ordinal + 10,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconRes = when (action) {
            CallAction.DECLINE, CallAction.HANGUP -> android.R.drawable.ic_menu_close_clear_cancel
            else -> android.R.drawable.ic_menu_call
        }

        return NotificationCompat.Action(iconRes, title, pendingIntent)
    }
}