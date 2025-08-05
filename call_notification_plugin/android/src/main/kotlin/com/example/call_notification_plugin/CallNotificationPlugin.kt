package com.example.call_notification_plugin

import android.content.Context
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** CallNotificationPlugin */
class CallNotificationPlugin: FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel
    private lateinit var context: Context
    private var callNotificationManager: CallNotificationManager? = null
    private var eventSink: EventChannel.EventSink? = null

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "call_notification_plugin")
        channel.setMethodCallHandler(this)
        
        eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "call_notification_plugin/events")
        eventChannel.setStreamHandler(this)
        
        callNotificationManager = CallNotificationManager(context) { action ->
            eventSink?.success(action)
        }
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "initialize" -> {
                val success = callNotificationManager?.initialize() ?: false
                result.success(success)
            }
            "showCallNotification" -> {
                val config = parseCallConfig(call.arguments as? Map<String, Any>)
                if (config != null) {
                    val success = callNotificationManager?.showCallNotification(config) ?: false
                    result.success(success)
                } else {
                    result.error("INVALID_CONFIG", "Invalid call configuration", null)
                }
            }
            "updateCallNotification" -> {
                val config = parseCallConfig(call.arguments as? Map<String, Any>)
                if (config != null) {
                    val success = callNotificationManager?.updateCallNotification(config) ?: false
                    result.success(success)
                } else {
                    result.error("INVALID_CONFIG", "Invalid call configuration", null)
                }
            }
            "dismissCallNotification" -> {
                val success = callNotificationManager?.dismissCallNotification() ?: false
                result.success(success)
            }
            "areCallNotificationsEnabled" -> {
                val enabled = callNotificationManager?.areCallNotificationsEnabled() ?: false
                result.success(enabled)
            }
            "requestPermissions" -> {
                val granted = callNotificationManager?.requestPermissions() ?: false
                result.success(granted)
            }
            "openNotificationSettings" -> {
                callNotificationManager?.openNotificationSettings()
                result.success(null)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private fun parseCallConfig(arguments: Map<String, Any>?): CallNotificationConfig? {
        if (arguments == null) return null
        
        return try {
            CallNotificationConfig(
                callerId = arguments["callerId"] as String,
                callerName = arguments["callerName"] as String,
                callerAvatar = arguments["callerAvatar"] as? String,
                isVideoCall = arguments["isVideoCall"] as Boolean,
                type = CallNotificationType.values()[(arguments["type"] as Int)],
                ringtoneUri = arguments["ringtoneUri"] as? String,
                enableVibration = arguments["enableVibration"] as? Boolean ?: true,
                customColor = arguments["customColor"] as? Int
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
        callNotificationManager?.cleanup()
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }
}