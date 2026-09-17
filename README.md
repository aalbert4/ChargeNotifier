# ChargeNotifier Application

A smart synchronization system between your Galaxy Phone and Galaxy watch that keeps you informed about your watch's status in real-time.

## Overview
Have you ever been out and about enjoying your day when you receive that dreadful vibration on your watch that alerts you that your battery is at 15% and you wished your phone had notified you earlier when it was at a certain percentage? With Charge Notifier you never have to worry about that anymore. ChargeNotifier ensures you never miss a beat with your wearable. By using a hybrid communication approach (Firebase Cloud + Wearable Data Layer), this app provides reliable notifications on your phone whenever your watch's status changes.

| Phone Dashboard | Settings | Wear OS Interface |
|:---:|:---:|:---:|
| ![Phone Dashboard](screenshots/Main_Screen.jpg) | ![Settings](screenshots/phone_settings.jpg) | ![Watch App](screenshots/wear_main.png) |

## Core Features

### Battery & Charging Monitoring
- **Live Tracking**: See your watch's exact battery percentage on your phone dashboard.
- **Charging Alerts**: Get notified immediately when your watch is placed on or removed from its charging puck.
- **Custom Low Battery Alert**: Set a specific threshold (e.g 30%) to receive a persistent "Low Battery" notification on your phone. This runs only once to avoid spam.

### Wrist Detection (On-Body)
- **Awareness**: Receive a notification on your phone the moment your watch is taken off your wrist or put back on.

### Customizable Experience
- **Selective Notifications**: Toggle Charging and On-Body notifications independently.
- **Auto-Dismiss Timer**: Choose how long status notifications stay on your phone (from 10 seconds up to 2 minutes) to keep your notification shade clean.
- **Foreground Reliability**: Uses Android Foreground Services on both devices to ensure background monitoring persists even during aggressive battery saving.
- **Modes and Routines plugin**: This is built for modes and routines from the ground up with notification support. You can trigger actions from your watch when your phone receives a notification for ChargeNotifier.   

## How It Works
1. **The Watch**: A background service monitors the battery level and the low-latency off-body sensor.
2. **The Bridge**: Updates are pushed to a Firebase Realtime Database and the local Wearable Data Layer simultaneously.
3. **The Phone**: A persistent listener service detects these changes and triggers highly-visible, self-dismissing notifications.

## Technical Details
- **Phone**: Beautiful UI Jetpack Compose (Material 3), Foreground Services, DataStore, Firebase.
- **Watch**: Wear Compose (Material 3), Sensor API, Battery Manager, Foreground Services.
- **Communication**: Firebase Realtime Database (Internet) + Google Play Services (Local).

---
*Optimized for Samsung Galaxy S26 Ultra and Galaxy Watch Ultra.*
