# Call Notification Plugin

A Flutter plugin for creating VoIP call-style notifications on Android, inspired by Signal's implementation. This plugin provides native Android call-style notifications with proper UI, actions, and audio/vibration support.

## Features

✅ **Call-Style Notifications** - Native Android call UI (API 29+) with fallback for older versions  
✅ **Multiple Call States** - Incoming, outgoing, connected, and connecting notifications  
✅ **Audio & Vibration** - Custom ringtones and vibration patterns  
✅ **Action Buttons** - Answer, decline, hangup with proper callbacks  
✅ **Permission Handling** - Notification permissions and settings management  
✅ **Signal-Inspired** - Based on Signal Android's robust notification system  

## Screenshots

The plugin creates native Android call notifications that appear as:
- **Full-screen incoming call UI** (Android 10+)
- **Heads-up notifications** with action buttons
- **Ongoing call status** in notification panel

## Installation

Add this to your package's `pubspec.yaml` file:

```yaml
dependencies:
  call_notification_plugin: ^0.0.1
```

## Android Setup

### 1. Permissions

Add these permissions to your `android/app/src/main/AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### 2. Minimum SDK

Set minimum SDK to 21 in `android/app/build.gradle`:

```gradle
android {
    defaultConfig {
        minSdkVersion 21
    }
}
```

## Usage

### Basic Setup

```dart
import 'package:call_notification_plugin/call_notification_plugin.dart';

// Initialize the plugin
await CallNotificationPlugin.initialize();

// Check permissions
bool enabled = await CallNotificationPlugin.areCallNotificationsEnabled();
if (!enabled) {
  await CallNotificationPlugin.requestPermissions();
}
```

### Listen to Call Actions

```dart
// Listen to user actions on notifications
CallNotificationPlugin.onCallAction.listen((action) {
  switch (action) {
    case CallAction.answer:
    case CallAction.answerAudio:
    case CallAction.answerVideo:
      // Handle call answered
      print('Call answered!');
      break;
    case CallAction.decline:
      // Handle call declined
      print('Call declined!');
      break;
    case CallAction.hangup:
      // Handle call ended
      print('Call ended!');
      break;
  }
});
```

### Show Incoming Call

```dart
// Simple incoming call
await CallNotificationPlugin.simulateIncomingCall(
  callerId: "user123",
  callerName: "John Doe",
  isVideoCall: false,
  enableVibration: true,
);

// Advanced configuration
final config = CallNotificationConfig(
  callerId: "user123",
  callerName: "John Doe",
  callerAvatar: base64EncodedImage, // Optional
  isVideoCall: true,
  type: CallNotificationType.incomingRinging,
  ringtoneUri: "content://settings/system/ringtone",
  enableVibration: true,
  customColor: 0xFF2196F3,
);

await CallNotificationPlugin.showCallNotification(config);
```

### Update Call State

```dart
// Show outgoing call
final outgoingConfig = CallNotificationConfig(
  callerId: "user123",
  callerName: "John Doe",
  isVideoCall: false,
  type: CallNotificationType.outgoingRinging,
);
await CallNotificationPlugin.showCallNotification(outgoingConfig);

// Update to connected state
await CallNotificationPlugin.simulateCallConnected(
  callerId: "user123",
  callerName: "John Doe",
  isVideoCall: false,
);

// Dismiss notification
await CallNotificationPlugin.dismissCallNotification();
```

## Call Notification Types

The plugin supports different notification states:

```dart
enum CallNotificationType {
  incomingRinging,    // Full-screen incoming call with answer/decline
  outgoingRinging,    // Outgoing call with cancel option
  established,        // Ongoing call with hangup option
  incomingConnecting, // Low-priority connecting state
}
```

## Call Actions

User interactions with notifications trigger these actions:

```dart
enum CallAction {
  answer,       // Generic answer action
  decline,      // Decline incoming call
  answerVideo,  // Answer as video call
  answerAudio,  // Answer as audio call
  hangup,       // End ongoing call
}
```

## Advanced Features

### Custom Avatar

Provide caller avatar as base64 encoded image:

```dart
String base64Avatar = base64Encode(imageBytes);

final config = CallNotificationConfig(
  callerId: "user123",
  callerName: "John Doe",
  callerAvatar: base64Avatar,
  // ... other properties
);
```

### Custom Ringtone

Use custom ringtone URI:

```dart
final config = CallNotificationConfig(
  callerId: "user123",
  callerName: "John Doe",
  ringtoneUri: "content://media/internal/audio/media/123",
  // ... other properties
);
```

### Permission Management

```dart
// Check if notifications are enabled
bool enabled = await CallNotificationPlugin.areCallNotificationsEnabled();

// Request permissions (Android 13+)
bool granted = await CallNotificationPlugin.requestPermissions();

// Open app notification settings
await CallNotificationPlugin.openNotificationSettings();
```

## Implementation Details

### Architecture

The plugin is built with these key components:

- **`CallNotificationBuilder`** - Creates Android notifications with proper styling
- **`CallNotificationManager`** - Manages notification lifecycle and audio/vibration
- **`CallNotificationReceiver`** - Handles notification action button presses
- **`CallNotificationPlugin`** - Flutter bridge and method channel communication

### Android Notification Channels

Two notification channels are created:

1. **High Priority Channel** (`calls_v3`) - For incoming calls with sound/vibration
2. **Low Priority Channel** (`call_status_v3`) - For call status updates

### Call Style Notifications

On Android 10+ (API 29), the plugin uses `NotificationCompat.CallStyle` for:
- Full-screen incoming call UI
- Native answer/decline buttons
- Proper call state handling

For older Android versions, regular notifications with action buttons are used.

### Audio & Vibration

The plugin includes:
- **Custom ringtone support** with fallback to system default
- **Vibration patterns** matching Signal's implementation
- **Audio attributes** set to `USAGE_NOTIFICATION_RINGTONE`
- **Automatic cleanup** when calls end

## Inspired by Signal

This plugin extracts and adapts key components from Signal Android's call notification system:

- **CallNotificationBuilder** - Based on Signal's notification builder
- **Notification channels** - Using Signal's channel configuration
- **Audio handling** - Inspired by Signal's IncomingRinger
- **Permission checks** - Following Signal's permission patterns
- **Call style support** - Using Signal's approach for modern Android versions

## Example App

See the `example/` directory for a complete demo app showing:

- Incoming voice/video call simulations
- Outgoing call simulation with auto-connect
- Permission handling
- Call action listening
- Notification state management

## Platform Support

- ✅ **Android** - Full support with native call-style notifications
- ❌ **iOS** - Not yet implemented (contributions welcome!)

## Contributing

Contributions are welcome! Please read the contributing guidelines and submit pull requests for any improvements.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- **Signal Messenger** - For the excellent call notification implementation that inspired this plugin
- **Flutter team** - For the robust plugin architecture
- **Android team** - For the CallStyle notification APIs