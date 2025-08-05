import 'dart:async';

import 'package:flutter/services.dart';

/// Enum for call notification types
enum CallNotificationType {
  incomingRinging,
  outgoingRinging,
  established,
  incomingConnecting,
}

/// Enum for call actions that can be triggered
enum CallAction {
  answer,
  decline,
  answerVideo,
  answerAudio,
  hangup,
}

/// Data class for call notification configuration
class CallNotificationConfig {
  final String callerId;
  final String callerName;
  final String? callerAvatar; // Base64 encoded image or null
  final bool isVideoCall;
  final CallNotificationType type;
  final String? ringtoneUri;
  final bool enableVibration;
  final int? customColor;

  const CallNotificationConfig({
    required this.callerId,
    required this.callerName,
    this.callerAvatar,
    required this.isVideoCall,
    required this.type,
    this.ringtoneUri,
    this.enableVibration = true,
    this.customColor,
  });

  Map<String, dynamic> toMap() {
    return {
      'callerId': callerId,
      'callerName': callerName,
      'callerAvatar': callerAvatar,
      'isVideoCall': isVideoCall,
      'type': type.index,
      'ringtoneUri': ringtoneUri,
      'enableVibration': enableVibration,
      'customColor': customColor,
    };
  }
}

/// Main plugin class for VoIP call notifications
class CallNotificationPlugin {
  static const MethodChannel _channel = MethodChannel('call_notification_plugin');
  static const EventChannel _eventChannel = EventChannel('call_notification_plugin/events');

  static Stream<CallAction>? _actionStream;

  /// Initialize the plugin and notification channels
  static Future<bool> initialize() async {
    try {
      final result = await _channel.invokeMethod('initialize');
      return result == true;
    } catch (e) {
      return false;
    }
  }

  /// Show a call notification
  static Future<bool> showCallNotification(CallNotificationConfig config) async {
    try {
      final result = await _channel.invokeMethod('showCallNotification', config.toMap());
      return result == true;
    } catch (e) {
      return false;
    }
  }

  /// Update an existing call notification
  static Future<bool> updateCallNotification(CallNotificationConfig config) async {
    try {
      final result = await _channel.invokeMethod('updateCallNotification', config.toMap());
      return result == true;
    } catch (e) {
      return false;
    }
  }

  /// Dismiss the call notification
  static Future<bool> dismissCallNotification() async {
    try {
      final result = await _channel.invokeMethod('dismissCallNotification');
      return result == true;
    } catch (e) {
      return false;
    }
  }

  /// Check if call notifications are enabled
  static Future<bool> areCallNotificationsEnabled() async {
    try {
      final result = await _channel.invokeMethod('areCallNotificationsEnabled');
      return result == true;
    } catch (e) {
      return false;
    }
  }

  /// Request call notification permissions
  static Future<bool> requestPermissions() async {
    try {
      final result = await _channel.invokeMethod('requestPermissions');
      return result == true;
    } catch (e) {
      return false;
    }
  }

  /// Open notification settings for the app
  static Future<void> openNotificationSettings() async {
    try {
      await _channel.invokeMethod('openNotificationSettings');
    } catch (e) {
      // Handle error silently
    }
  }

  /// Listen to call action events (answer, decline, etc.)
  static Stream<CallAction> get onCallAction {
    _actionStream ??= _eventChannel
        .receiveBroadcastStream()
        .map((event) => _parseCallAction(event))
        .where((action) => action != null)
        .cast<CallAction>();
    return _actionStream!;
  }

  static CallAction? _parseCallAction(dynamic event) {
    if (event is String) {
      switch (event) {
        case 'answer':
          return CallAction.answer;
        case 'decline':
          return CallAction.decline;
        case 'answerVideo':
          return CallAction.answerVideo;
        case 'answerAudio':
          return CallAction.answerAudio;
        case 'hangup':
          return CallAction.hangup;
      }
    }
    return null;
  }

  /// Simulate an incoming call notification
  static Future<bool> simulateIncomingCall({
    required String callerId,
    required String callerName,
    String? callerAvatar,
    bool isVideoCall = false,
    String? ringtoneUri,
    bool enableVibration = true,
  }) async {
    final config = CallNotificationConfig(
      callerId: callerId,
      callerName: callerName,
      callerAvatar: callerAvatar,
      isVideoCall: isVideoCall,
      type: CallNotificationType.incomingRinging,
      ringtoneUri: ringtoneUri,
      enableVibration: enableVibration,
    );
    return await showCallNotification(config);
  }

  /// Simulate transitioning call to connected state
  static Future<bool> simulateCallConnected({
    required String callerId,
    required String callerName,
    String? callerAvatar,
    bool isVideoCall = false,
  }) async {
    final config = CallNotificationConfig(
      callerId: callerId,
      callerName: callerName,
      callerAvatar: callerAvatar,
      isVideoCall: isVideoCall,
      type: CallNotificationType.established,
      enableVibration: false,
    );
    return await updateCallNotification(config);
  }
}