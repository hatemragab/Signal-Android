package com.example.call_notification_plugin

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/**
 * Manages call notifications and their lifecycle
 * Inspired by Signal's ActiveCallManager and IncomingRinger
 */
class CallNotificationManager(
    private val context: Context,
    private val onActionCallback: (String) -> Unit
) {
    private val notificationBuilder = CallNotificationBuilder(context) { action ->
        onActionCallback(action.value)
    }
    
    private val notificationManager = NotificationManagerCompat.from(context)
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    
    private var currentNotificationId: Int = -1
    private var mediaPlayer: MediaPlayer? = null
    private var currentConfig: CallNotificationConfig? = null
    
    companion object {
        private var instance: CallNotificationManager? = null
        
        fun getInstance(): CallNotificationManager? = instance
        
        private val VIBRATE_PATTERN = longArrayOf(0, 1000, 1000)
    }

    init {
        instance = this
    }

    /**
     * Initialize notification channels and setup
     */
    fun initialize(): Boolean {
        return try {
            notificationBuilder.createNotificationChannels()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Show a call notification
     */
    fun showCallNotification(config: CallNotificationConfig): Boolean {
        return try {
            currentConfig = config
            val notification = notificationBuilder.getCallNotification(config)
            val notificationId = notificationBuilder.getNotificationId(config.type)
            
            // Cancel previous notification if different ID
            if (currentNotificationId != -1 && currentNotificationId != notificationId) {
                notificationManager.cancel(currentNotificationId)
            }
            
            currentNotificationId = notificationId
            notificationManager.notify(notificationId, notification)
            
            // Start ringing and vibration for incoming calls
            if (config.type == CallNotificationType.INCOMING_RINGING) {
                startRingerAndVibration(config)
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update an existing call notification
     */
    fun updateCallNotification(config: CallNotificationConfig): Boolean {
        return try {
            // Stop ringing when transitioning from incoming to other states
            if (currentConfig?.type == CallNotificationType.INCOMING_RINGING && 
                config.type != CallNotificationType.INCOMING_RINGING) {
                stopRingerAndVibration()
            }
            
            currentConfig = config
            val notification = notificationBuilder.getCallNotification(config)
            val notificationId = notificationBuilder.getNotificationId(config.type)
            
            currentNotificationId = notificationId
            notificationManager.notify(notificationId, notification)
            
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Dismiss the call notification
     */
    fun dismissCallNotification(): Boolean {
        return try {
            if (currentNotificationId != -1) {
                notificationManager.cancel(currentNotificationId)
                currentNotificationId = -1
            }
            stopRingerAndVibration()
            currentConfig = null
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if call notifications are enabled
     */
    fun areCallNotificationsEnabled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = notificationManager.getNotificationChannel(CallNotificationBuilder.CALLS_CHANNEL)
                channel?.importance == NotificationManager.IMPORTANCE_HIGH
            } else {
                NotificationManagerCompat.from(context).areNotificationsEnabled()
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Request notification permissions (mainly for Android 13+)
     */
    fun requestPermissions(): Boolean {
        return try {
            // For Android 13+ we would need to request POST_NOTIFICATIONS permission
            // For this simulation, we'll just check if notifications are enabled
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Open notification settings for the app
     */
    fun openNotificationSettings() {
        try {
            val intent = Intent().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                } else {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:${context.packageName}")
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    /**
     * Handle notification action button presses
     */
    fun handleNotificationAction(action: CallAction) {
        // Stop ringing for any action
        stopRingerAndVibration()
        
        when (action) {
            CallAction.DECLINE -> {
                // Decline incoming call - dismiss notification
                dismissCallNotification()
            }
            CallAction.HANGUP -> {
                // End ongoing call - dismiss notification
                dismissCallNotification()
            }
            CallAction.ANSWER,
            CallAction.ANSWER_AUDIO,
            CallAction.ANSWER_VIDEO -> {
                // Answer call - keep notification but transition to connected state
                // The notification will be updated by Flutter to show ongoing call
                // Don't dismiss here, let Flutter control the transition
            }
        }
        
        // Notify Flutter about the action
        onActionCallback(action.value)
    }

    /**
     * Start ringing and vibration for incoming calls
     */
    private fun startRingerAndVibration(config: CallNotificationConfig) {
        // Start ringtone
        config.ringtoneUri?.let { ringtoneUriString ->
            try {
                val ringtoneUri = Uri.parse(ringtoneUriString)
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, ringtoneUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                            .build()
                    )
                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                // Use default ringtone or no sound if custom ringtone fails
                mediaPlayer = null
            }
        }

        // Start vibration
        if (config.enableVibration && vibrator?.hasVibrator() == true) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(VIBRATE_PATTERN, 0))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(VIBRATE_PATTERN, 0)
                }
            } catch (e: Exception) {
                // Handle vibration error silently
            }
        }
    }

    /**
     * Stop ringing and vibration
     */
    private fun stopRingerAndVibration() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            } catch (e: Exception) {
                // Handle error silently
            }
            mediaPlayer = null
        }

        vibrator?.cancel()
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        stopRingerAndVibration()
        dismissCallNotification()
        instance = null
    }
}