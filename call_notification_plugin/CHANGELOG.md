# Changelog

## [0.0.1] - 2024-12-19

### Added
- Initial release of call notification plugin
- VoIP call-style notifications for Android
- Support for incoming, outgoing, connected, and connecting call states
- Native Android call UI for API 29+ with fallback for older versions
- Audio and vibration support with custom ringtones
- Action buttons (answer, decline, hangup) with proper callbacks
- Permission handling for notifications
- Avatar support with base64 encoded images
- Notification channel management
- Flutter method channel bridge
- Example app demonstrating all features
- Comprehensive documentation

### Features
- **CallNotificationBuilder** - Creates Android notifications with proper styling
- **CallNotificationManager** - Manages notification lifecycle and audio/vibration  
- **CallNotificationReceiver** - Handles notification action button presses
- **Permission management** - Request and check notification permissions
- **Multiple call types** - Voice and video call support
- **Signal-inspired** - Based on Signal Android's robust implementation

### Platform Support
- ✅ Android (API 21+)
- ❌ iOS (not yet implemented)

### Dependencies
- Flutter SDK >=2.5.0
- Dart SDK >=2.17.0
- Android compileSdk 33
- Kotlin 1.7.10