import 'package:flutter/material.dart';
import 'package:call_notification_plugin/call_notification_plugin.dart';
import 'dart:async';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Call Notification Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: CallNotificationDemo(),
    );
  }
}

class CallNotificationDemo extends StatefulWidget {
  @override
  _CallNotificationDemoState createState() => _CallNotificationDemoState();
}

class _CallNotificationDemoState extends State<CallNotificationDemo> {
  StreamSubscription<CallAction>? _actionSubscription;
  String _lastAction = 'None';
  bool _isInitialized = false;
  bool _notificationsEnabled = false;
  bool _hasActiveCall = false;
  String _callState = 'None'; // Track call state for better UI feedback

  @override
  void initState() {
    super.initState();
    _initializePlugin();
    _listenToCallActions();
  }

  Future<void> _initializePlugin() async {
    try {
      final initialized = await CallNotificationPlugin.initialize();
      final enabled = await CallNotificationPlugin.areCallNotificationsEnabled();
      
      setState(() {
        _isInitialized = initialized;
        _notificationsEnabled = enabled;
      });
    } catch (e) {
      print('Error initializing plugin: $e');
    }
  }

  void _listenToCallActions() {
    _actionSubscription = CallNotificationPlugin.onCallAction.listen((action) {
      setState(() {
        _lastAction = action.toString().split('.').last;
      });
      
      _handleCallAction(action);
    });
  }

  void _handleCallAction(CallAction action) {
    switch (action) {
      case CallAction.answer:
      case CallAction.answerAudio:
      case CallAction.answerVideo:
        _showSnackBar('Call answered! Transitioning to connected...', Colors.green);
        setState(() {
          _callState = 'Connecting...';
        });
        _simulateCallConnected();
        break;
      case CallAction.decline:
        _showSnackBar('Call declined!', Colors.red);
        setState(() {
          _hasActiveCall = false;
          _callState = 'None';
        });
        break;
      case CallAction.hangup:
        _showSnackBar('Call ended!', Colors.orange);
        setState(() {
          _hasActiveCall = false;
          _callState = 'None';
        });
        break;
    }
  }

  void _showSnackBar(String message, Color color) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: color,
        duration: Duration(seconds: 2),
      ),
    );
  }

  Future<void> _simulateIncomingVoiceCall() async {
    final success = await CallNotificationPlugin.simulateIncomingCall(
      callerId: "user123",
      callerName: "John Doe",
      isVideoCall: false,
      enableVibration: true,
    );
    
    if (success) {
      setState(() {
        _hasActiveCall = true;
        _callState = 'Incoming';
      });
      _showSnackBar('Incoming voice call simulated!', Colors.blue);
    } else {
      _showSnackBar('Failed to show notification', Colors.red);
    }
  }

  Future<void> _simulateIncomingVideoCall() async {
    final success = await CallNotificationPlugin.simulateIncomingCall(
      callerId: "user456",
      callerName: "Jane Smith",
      isVideoCall: true,
      enableVibration: true,
    );
    
    if (success) {
      setState(() {
        _hasActiveCall = true;
        _callState = 'Incoming';
      });
      _showSnackBar('Incoming video call simulated!', Colors.blue);
    } else {
      _showSnackBar('Failed to show notification', Colors.red);
    }
  }

  Future<void> _simulateCallConnected() async {
    final success = await CallNotificationPlugin.simulateCallConnected(
      callerId: "user123",
      callerName: "John Doe",
      isVideoCall: false,
    );
    
    if (success) {
      setState(() {
        _hasActiveCall = true;
        _callState = 'Connected'; // This is the ongoing call state
      });
      _showSnackBar('Call connected! Notification will remain visible.', Colors.green);
    }
  }

  Future<void> _simulateOutgoingCall() async {
    final config = CallNotificationConfig(
      callerId: "user789",
      callerName: "Bob Wilson",
      isVideoCall: false,
      type: CallNotificationType.outgoingRinging,
      enableVibration: false,
    );
    
    final success = await CallNotificationPlugin.showCallNotification(config);
    
    if (success) {
      setState(() {
        _hasActiveCall = true;
        _callState = 'Outgoing';
      });
      _showSnackBar('Outgoing call simulated!', Colors.orange);
      
      // Simulate connection after 3 seconds
      Timer(Duration(seconds: 3), () {
        _simulateCallConnected();
      });
    } else {
      _showSnackBar('Failed to show notification', Colors.red);
    }
  }

  Future<void> _endCall() async {
    final success = await CallNotificationPlugin.dismissCallNotification();
    
    if (success) {
      setState(() {
        _hasActiveCall = false;
        _callState = 'None';
      });
      _showSnackBar('Call ended!', Colors.grey);
    }
  }

  Future<void> _requestPermissions() async {
    final granted = await CallNotificationPlugin.requestPermissions();
    setState(() {
      _notificationsEnabled = granted;
    });
    
    if (granted) {
      _showSnackBar('Permissions granted!', Colors.green);
    } else {
      _showSnackBar('Permissions denied', Colors.red);
    }
  }

  @override
  void dispose() {
    _actionSubscription?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Call Notification Demo'),
        backgroundColor: Colors.blue,
      ),
      body: Padding(
        padding: EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Status Card
            Card(
              child: Padding(
                padding: EdgeInsets.all(16),
                child: Column(
                  children: [
                    Text(
                      'Plugin Status',
                      style: Theme.of(context).textTheme.headlineSmall,
                    ),
                    SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text('Initialized:'),
                        Icon(
                          _isInitialized ? Icons.check_circle : Icons.error,
                          color: _isInitialized ? Colors.green : Colors.red,
                        ),
                      ],
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text('Notifications Enabled:'),
                        Icon(
                          _notificationsEnabled ? Icons.notifications_active : Icons.notifications_off,
                          color: _notificationsEnabled ? Colors.green : Colors.red,
                        ),
                      ],
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text('Active Call:'),
                        Icon(
                          _hasActiveCall ? Icons.phone : Icons.phone_disabled,
                          color: _hasActiveCall ? Colors.green : Colors.grey,
                        ),
                      ],
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text('Call State:'),
                        Text(
                          _callState,
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: _callState == 'Connected' ? Colors.green : 
                                   _callState == 'Incoming' ? Colors.blue :
                                   _callState == 'Outgoing' ? Colors.orange : Colors.grey,
                          ),
                        ),
                      ],
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text('Last Action:'),
                        Text(
                          _lastAction,
                          style: TextStyle(fontWeight: FontWeight.bold),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            
            SizedBox(height: 16),
            
            // Permission Button
            if (!_notificationsEnabled)
              ElevatedButton.icon(
                onPressed: _requestPermissions,
                icon: Icon(Icons.settings),
                label: Text('Request Permissions'),
                style: ElevatedButton.styleFrom(backgroundColor: Colors.orange),
              ),
            
            SizedBox(height: 16),
            
            // Call Simulation Buttons
            Text(
              'Simulate Calls',
              style: Theme.of(context).textTheme.headlineSmall,
            ),
            SizedBox(height: 8),
            
            ElevatedButton.icon(
              onPressed: _isInitialized && !_hasActiveCall ? _simulateIncomingVoiceCall : null,
              icon: Icon(Icons.phone),
              label: Text('Incoming Voice Call'),
              style: ElevatedButton.styleFrom(backgroundColor: Colors.green),
            ),
            
            SizedBox(height: 8),
            
            ElevatedButton.icon(
              onPressed: _isInitialized && !_hasActiveCall ? _simulateIncomingVideoCall : null,
              icon: Icon(Icons.videocam),
              label: Text('Incoming Video Call'),
              style: ElevatedButton.styleFrom(backgroundColor: Colors.blue),
            ),
            
            SizedBox(height: 8),
            
            ElevatedButton.icon(
              onPressed: _isInitialized && !_hasActiveCall ? _simulateOutgoingCall : null,
              icon: Icon(Icons.call_made),
              label: Text('Outgoing Call'),
              style: ElevatedButton.styleFrom(backgroundColor: Colors.orange),
            ),
            
            SizedBox(height: 16),
            
            // Call Control Button
            if (_hasActiveCall)
              Column(
                children: [
                  if (_callState == 'Connected')
                    Container(
                      padding: EdgeInsets.all(12),
                      margin: EdgeInsets.only(bottom: 8),
                      decoration: BoxDecoration(
                        color: Colors.green.withOpacity(0.1),
                        border: Border.all(color: Colors.green),
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Row(
                        children: [
                          Icon(Icons.info_outline, color: Colors.green),
                          SizedBox(width: 8),
                          Expanded(
                            child: Text(
                              'Connected call notification persists until you hang up!',
                              style: TextStyle(color: Colors.green, fontWeight: FontWeight.w500),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ElevatedButton.icon(
                    onPressed: _endCall,
                    icon: Icon(Icons.call_end),
                    label: Text('End Call'),
                    style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
                  ),
                ],
              ),
            
            Spacer(),
            
            // Settings Button
            ElevatedButton.icon(
              onPressed: () => CallNotificationPlugin.openNotificationSettings(),
              icon: Icon(Icons.settings),
              label: Text('Open Notification Settings'),
              style: ElevatedButton.styleFrom(backgroundColor: Colors.grey),
            ),
          ],
        ),
      ),
    );
  }
}