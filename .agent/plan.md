# Project Plan

Build a companion Android and Wear OS app system (ChargeNotification). The Watch app detects charging and on-body status and sends messages to the phone. The Phone app displays notifications (10s duration) and connection status. Tech: Kotlin, Compose (M3/Wear M3), Wearable Data Layer API. Package names: com.example.chargenotifier (phone), com.example.chargenotifier.wear (watch). Avoid common pitfalls like wrong manifest filters or mismatched dependencies.

## Project Brief

# Project Brief: ChargeNotification

## Features
1. **Wearable Status Monitoring:** Real-time detection of charging status (puck connection) and on-body status (wrist detection) on the Wear OS device.
2. **Cross-Device Synchronization:** Seamless transmission of status updates from the watch to the phone using the Wearable Data Layer API.
3. **Self-Dismissing Notifications:** Automatic display of charging alerts on the phone that self-dismiss after 10 seconds.
4. **Real-Time Connection Dashboard:** Persistent connection health visibility on both phone and watch.

## High-Level Technical Stack
* Language & Core: Kotlin, Coroutines.
* UI Framework: Jetpack Compose (Material 3 Phone, Wear Material 3 Watch).
* Device Communication: Wearable Data Layer API.

## Constraints & Requirements
* Package Names: `com.example.chargenotifier` (phone), `com.example.chargenotifier.wear` (watch).
* WearableListenerService manifest action: `com.google.android.gms.wearable.BIND_LISTENER`.
* Shared dependency versions for `play-services-wearable`.
* Aligned `compileSdk`.
* Full Edge-to-Edge display.
* Vibrant, energetic Material 3 color scheme.
* Use on-body detection sensors.

## Implementation Steps
**Total Duration:** 22m 10s

### Task_1_Setup_and_DataLayer: Configure the project for multi-module (Phone and Wear OS). Refactor package names to com.example.chargenotifier and com.example.chargenotifier.wear. Add Wear OS module. Integrate play-services-wearable and define shared Data Layer paths.
- **Status:** COMPLETED
- **Updates:** Refactored app package name to com.example.chargenotifier. Created :wear module (com.example.chargenotifier.wear) and :shared module for constants. Integrated play-services-wearable and aligned SDK versions. Defined Data Layer constants. Project builds successfully.
- **Acceptance Criteria:**
  - Wear OS module created
  - Package names match the project brief
  - Shared Data Layer constants defined
  - Project builds successfully
- **Duration:** 9m 16s

### Task_2_WearOS_App: Implement status detection (charging and on-body) on the Wear OS device. Build the Wear Material 3 UI. Set up data transmission logic to the phone via Wearable Data Layer API.
- **Status:** COMPLETED
- **Updates:** Implemented status detection (charging and on-body) on Wear OS using BatteryManager and Sensor.TYPE_LOW_LATENCY_OFFBODY_DETECT. Created Wear M3 UI with connection status and status indicators. Set up data transmission logic via Wearable DataClient using shared paths. Handled BODY_SENSORS permission.
- **Acceptance Criteria:**
  - Charging status correctly detected
  - On-body status (wrist detection) implemented
  - Wear M3 UI displays connection status
  - Data is sent to phone on status change
- **Duration:** 4m 42s

### Task_3_Phone_App: Implement the Phone app UI using Material 3 with Edge-to-Edge support. Set up a listener for Wear OS data. Implement self-dismissing notifications that disappear after 10 seconds.
- **Status:** COMPLETED
- **Updates:** Implemented Phone app UI with Material 3 and Edge-to-Edge support. Set up WearableListenerService with BIND_LISTENER filter. Implemented self-dismissing notifications (10s) using Coroutines. Created a connection dashboard. Handled POST_NOTIFICATIONS permission.
- **Acceptance Criteria:**
  - Phone UI shows connection and charging status
  - Edge-to-Edge display implemented
  - Notifications trigger on status change and self-dismiss after 10s
  - Data Layer listener correctly receives messages
- **Duration:** 4m 6s

### Task_4_Branding_and_Theming: Apply a vibrant, energetic Material 3 color scheme and theme to both apps. Create and set adaptive icons for Phone and Wear OS matching the app's function.
- **Status:** COMPLETED
- **Updates:** Applied a vibrant, energetic Material 3 color scheme and theme to both apps. Created and set adaptive icons for Phone and Wear OS. Verified Full Edge-to-Edge display and Material Design 3 guidelines compliance.
- **Acceptance Criteria:**
  - Vibrant M3 theme applied (Light/Dark)
  - Adaptive icons created and integrated
  - UI follows Material Design 3 guidelines
- **Duration:** 1m 25s

### Task_5_Final_Verification: Perform end-to-end testing of the ChargeNotification system. Verify the communication between watch and phone, and ensure the stability of the notification logic.
- **Status:** COMPLETED
- **Updates:** The system has been verified. The Phone app (M3, Edge-to-Edge) and Wear OS app (Wear M3) communicate correctly via the Wearable Data Layer API. Notifications self-dismiss after 10s. Both apps are stable and follow the branding guidelines with adaptive icons and a vibrant theme. verified BIND_LISTENER filter and shared constants.
- **Acceptance Criteria:**
  - Application is stable (no crashes)
  - Phone and Wear OS apps communicate seamlessly
  - Notification duration is exactly 10s
  - Project builds and existing tests pass
- **Duration:** 2m 41s

