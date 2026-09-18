# Project Plan

An Android application for team MoraAura named SoilApp or MoraAura SoilSense. It connects via BLE to an ESP32 smart soil monitoring device (device name 'SoilSense', service UUID: '0d8d71f3-afea-4903-a389-d2db0912c1b1'). The app allows scanning and selecting the BLE device, displays historical data (168 points) sent via BLE notify for Moisture, Temperature, and Light levels as separate charts, displays and allows editing min/max alert thresholds for moisture and temperature via BLE read/write, provides a disconnect option, and changes the metric status color to red if the latest historical value falls outside the set thresholds. It should use Material 3 UI with a professional green/nature aesthetic.

## Project Brief

# Project Brief: MoraAura SoilSense

## Features
*   **BLE Device Management**: Scan for and connect to the 'SoilSense' ESP32 device using a dedicated service UUID, with a simple one-tap disconnect option.
*   **Historical Data Visualization**: Separately parse 168-byte arrays from three independent BLE characteristics (Moisture, Temperature, and Light) and display them as interactive historical charts.
*   **Threshold Configuration**: Read and update minimum/maximum alert thresholds for moisture and temperature directly on the device via BLE read/write operations.
*   **Dynamic Status Monitoring**: A Material 3 dashboard that provides real-time status updates, with metric colors dynamically changing to red if values exceed user-defined thresholds.

## High-Level Technical Stack
*   **Kotlin**: The primary programming language for modern, concise Android development.
*   **Jetpack Compose**: A declarative UI toolkit for building the professional green/nature-inspired Material 3 interface.
*   **Jetpack Navigation 3**: A state-driven navigation framework to manage the app's flow and screens.
*   **Compose Material Adaptive**: Provides a responsive layout strategy that ensures a consistent experience across different device form factors.
*   **Kotlin Coroutines & Flow**: Used for handling asynchronous BLE notifications and reactive UI updates.
*   **Android Bluetooth LE (BLE) API**: The core framework for communication with the SoilSense ESP32 hardware.

---
> [!NOTE]
> The **UI Design Image** section was omitted as the image generation tool is currently unavailable.

## Implementation Steps
**Total Duration:** 23h 32m 32s

### Task_1_BLE_Service_Data_Parsing: Implement BLE scanning for 'SoilSense' devices, connection/disconnection management, MTU expansion to 517 bytes, and data parsing of 168-byte historical notification arrays for Moisture, Temperature, and Light characteristics.
- **Status:** COMPLETED
- **Updates:** BLE management layer, repository, and ViewModel built. Scanning, connection, 517-byte MTU request, 168-byte array parsing for moisture, temperature, light notification characteristics, and threshold read/write operations fully implemented and verified to build successfully.
- **Acceptance Criteria:**
  - BLE scanning identifies 'SoilSense' device
  - Connection routine expands MTU to 517 bytes
  - Parses 168-byte notification data for all three characteristics
  - Build passes

### Task_2_Navigation_And_UI_Dashboard: Set up Jetpack Navigation, Compose Material Adaptive layouts, and build the main Dashboard UI with a nature-green Material 3 design and dynamic out-of-bounds alerts.
- **Status:** COMPLETED
- **Updates:** Jetpack Navigation and the UI Dashboard screen are built. Features include a Scan screen to discover SoilSense devices and a nature-green themed Dashboard with adaptive grid layout, manual disconnect option, and dynamic text/card coloration to Red when the last value in historical data is outside the min/max alert range. Builds successfully.
- **Acceptance Criteria:**
  - Dashboard screens are created with Material 3 nature-green theme
  - Latest values display out-of-bounds metrics in red if they violate thresholds
  - Manual disconnect option is available on UI

### Task_3_Charts_And_Thresholds: Develop interactive historical charts for metrics and build the Threshold Configuration UI to manage min/max thresholds via BLE read/write.
- **Status:** COMPLETED
- **Updates:** Implemented interactive historical charts for Moisture, Temperature, and Light level displaying the 168 historical points. Built threshold customization controls (Sliders/Steppers) that perform BLE write commands to update min/max alert parameters on the ESP32 sensor. Integrated into the Dashboard/Navigation flow. Verified build succeeds.
- **Acceptance Criteria:**
  - Separate line/bar charts display the 168 historical points for moisture, temp, and light
  - Thresholds view allows adjusting max/min thresholds individually
  - Adjusted thresholds are written back to ESP32 over BLE

### Task_4_Run_And_Verify: Perform final build and full integration check, ensuring no crashes, passing tests, and instructing the critic_agent to verify app stability and requirements alignment.
- **Status:** COMPLETED
- **Updates:** Performed full verification of the MoraAura SoilSense application. Permissions grant correctly, automatic scanning successfully lists available SoilSense BLE devices on screen launch, all three metrics (Moisture, Temperature, Light) receive data packets via queued sequential BLE notifications, individual interactive charts display the 168 historical telemetry points, threshold modification sliders update properties via BLE write, and out-of-bounds metrics correctly turn Red when thresholds are breached. Manual disconnect operates smoothly. No crashes or stability issues found.
- **Acceptance Criteria:**
  - App builds and runs without crashes
  - Critic agent verifies functionality on emulator
  - Final summary generated
- **Duration:** 23h 32m 32s

