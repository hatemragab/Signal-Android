# Testing Guide - Call Notification Plugin

## 🧪 Testing the Persistent Connected Call Feature

This guide explains how to test the new persistent connected call notification behavior.

### ✅ **Test Scenario 1: Incoming Call → Answer → Connected**

1. **Start the example app**
   ```bash
   cd example
   flutter run
   ```

2. **Simulate incoming call**
   - Tap "Incoming Voice Call" or "Incoming Video Call"
   - ✅ **Expected**: Notification appears with answer/decline buttons
   - ✅ **Expected**: Call state shows "Incoming"
   - ✅ **Expected**: Ringtone and vibration start (if enabled)

3. **Answer the call from notification**
   - Tap "Answer" button in the notification
   - ✅ **Expected**: Ringtone stops
   - ✅ **Expected**: App shows "Call answered! Transitioning to connected..."
   - ✅ **Expected**: Call state changes to "Connecting..." then "Connected"
   - ✅ **Expected**: Notification persists and shows "Hang Up" button
   - ✅ **Expected**: Green info box appears: "Connected call notification persists until you hang up!"

4. **Verify persistence**
   - Try swiping away notification (should return)
   - Navigate away from app
   - ✅ **Expected**: Notification remains visible in status bar
   - ✅ **Expected**: Notification shows ongoing call information

5. **End the call**
   - Tap "Hang Up" in notification OR "End Call" in app
   - ✅ **Expected**: Notification disappears
   - ✅ **Expected**: Call state returns to "None"

### ✅ **Test Scenario 2: Incoming Call → Decline**

1. **Simulate incoming call**
   - Tap "Incoming Voice Call"
   - ✅ **Expected**: Notification appears with answer/decline buttons

2. **Decline the call**
   - Tap "Decline" button in notification
   - ✅ **Expected**: Notification immediately disappears
   - ✅ **Expected**: App shows "Call declined!"
   - ✅ **Expected**: Call state returns to "None"

### ✅ **Test Scenario 3: Outgoing Call → Auto Connect**

1. **Simulate outgoing call**
   - Tap "Outgoing Call"
   - ✅ **Expected**: Notification shows "Establishing call..." with cancel button
   - ✅ **Expected**: Call state shows "Outgoing"

2. **Wait for auto-connect (3 seconds)**
   - ✅ **Expected**: Notification automatically updates to connected state
   - ✅ **Expected**: Call state changes to "Connected"
   - ✅ **Expected**: Notification shows "Hang Up" button
   - ✅ **Expected**: Green persistence info appears

3. **End the call**
   - ✅ **Expected**: Notification disappears when ended

### ✅ **Test Scenario 4: Call Action Event Streaming**

1. **Monitor call actions**
   - Watch the "Last Action" field in the app
   - ✅ **Expected**: Shows real-time action updates

2. **Test all actions**
   - Answer → Shows "answerAudio" or "answerVideo"
   - Decline → Shows "decline"
   - Hang Up → Shows "hangup"

### 🔧 **Testing on Different Android Versions**

#### **Android 10+ (API 29+)**
- ✅ Uses `CallStyle` notifications (full-screen incoming calls)
- ✅ Native answer/decline buttons
- ✅ Proper ongoing call display

#### **Android < 10 (API < 29)**
- ✅ Falls back to regular notifications with action buttons
- ✅ All functionality preserved
- ✅ Persistent behavior works the same

### 📱 **Manual Testing Checklist**

- [ ] Incoming call notification appears
- [ ] Answer button works and transitions to connected
- [ ] Connected notification persists
- [ ] Connected notification shows hangup button
- [ ] Decline button works and dismisses notification
- [ ] Hangup button works and dismisses notification
- [ ] Outgoing call transitions to connected automatically
- [ ] Call state indicators work correctly
- [ ] Ringtone and vibration work (if enabled)
- [ ] Permissions are handled correctly
- [ ] Multiple call simulations work
- [ ] App state sync works with notification actions

### 🐛 **Expected Behavior vs. Old Behavior**

#### **✅ NEW BEHAVIOR (v0.0.2+)**
```
Incoming Call → Answer → Connected (PERSISTENT) → Manual Hangup → Dismissed
```

#### **❌ OLD BEHAVIOR (v0.0.1)**
```
Incoming Call → Answer → Dismissed (WRONG!)
```

### 🚀 **Performance Testing**

1. **Memory**: Connected notifications should not leak memory
2. **Battery**: Persistent notifications should not drain battery excessively
3. **Stability**: Multiple call cycles should work reliably

### 📋 **Testing Notes**

- Test on both physical devices and emulators
- Test with different notification permission states
- Test with do-not-disturb mode enabled
- Test with custom ringtones
- Test with different avatar configurations
- Verify behavior when app is backgrounded/foregrounded

---

## 🎯 **Key Improvement Verified**

**✅ PERSISTENT CONNECTED CALLS**: The notification now correctly remains visible for ongoing calls until manually ended, matching the behavior of professional VoIP applications like Signal, WhatsApp, and Telegram.

This ensures users always know when they have an active call and can easily hang up from the notification panel.