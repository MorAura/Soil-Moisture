# Smart Soil Condition Monitoring

An ESP32-powered system for healthier plants and more precise care.

> Sense the environment. Respond with confidence.

## About MorAura

Monitoring plants is a tedious, labour-intensive task. Consistent monitoring can reduce the likelihood of plants dying out and may improve crop yield.

MorAura is designed to monitor plant conditions and help keep them within ideal ranges, reducing guesswork and the need for constant human monitoring.

## Project Goals

- Accurately measure plant conditions.
- Reduce guesswork in plant care.
- Reduce the need for constant human monitoring.

## System Architecture

The project is being developed in two versions. Version 1 sends sensor readings from the ESP32 to the Android app over Bluetooth Low Energy (BLE). Version 2 is in progress and is intended to send readings over Wi-Fi to a server.

```text
Soil moisture sensor ─┐
Light sensor         ─┼──> ESP32 ──BLE (Version 1)──> Android app
Temperature sensor   ─┘       └──Wi-Fi (Version 2, in progress)──> Server
```

### Sensors

- Soil moisture
- Light intensity
- Temperature

## Example Monitoring Profile

The following values are illustrative targets for the prototype. They should be calibrated for the plant species and real growing environment.

| Condition | Example target range |
| --- | --- |
| Soil moisture | 40–65% |
| Light | 1,000–10,000 lux |
| Temperature | 20–28°C |

When a reading leaves its target range, the system should notify the user that the condition is out of range.

## Build and Field Test Plan

1. Research the problem and suitable sensors. (Complete)
2. Develop the first ESP32 firmware and Android BLE app. (Implemented; hardware validation and calibration remain ongoing.)
3. Develop Wi-Fi data logging and server communication. (In progress.)
4. Visit plantations to survey users and collect feedback. (Planned.)
5. Calibrate the monitoring ranges and test the system at real plantation sites. (Planned.)

The field visits will help confirm that the system is practical for real-world scenarios.

## Repository Structure

```text
.
├── README.md
├── Android_Apps/
│   └── SoilApp/             # Android app (Kotlin, Jetpack Compose)
└── Sensor_Code/
	├── Code_v1/Code_v1.ino # BLE firmware
	└── Code_v2/Code_v2.ino # Wi-Fi and local logging work in progress
```

## Getting Started

### Version 1: BLE Prototype

The Android project is in [`Android_Apps/SoilApp/`](Android_Apps/SoilApp/). Open that directory in Android Studio to build and run the app on an Android device with BLE support. The ESP32 BLE firmware is [`Sensor_Code/Code_v1/Code_v1.ino`](Sensor_Code/Code_v1/Code_v1.ino); open it in an Arduino-compatible ESP32 development environment, select the matching board, and flash it to the board.

The firmware currently reads soil moisture on `A3`, light on `A2`, and temperature from the ESP32's internal temperature sensor. Confirm the pin mapping and board support for the specific ESP32 before wiring or flashing. The internal temperature sensor is being used for testing and may not represent ambient plant temperature accurately.

The app can scan for the `SoilSense` BLE service, show moisture, temperature, and light readings, plot received history, and configure minimum and maximum moisture and temperature thresholds. The firmware keeps up to 168 samples for each metric and sends those histories to the app over BLE. Light is currently mapped to a 0–100 sensor scale, not converted to calibrated lux; the app's lux label is provisional.

### Version 2: Wi-Fi and Server (In Progress)

The current Wi-Fi sketch is [`Sensor_Code/Code_v2/Code_v2.ino`](Sensor_Code/Code_v2/Code_v2.ino). It averages sensor readings, synchronizes time using NTP, and appends timestamped readings to a local LittleFS file. The server synchronization function is currently a placeholder; end-to-end upload and server integration are not implemented yet. Configure Wi-Fi credentials locally before testing, and do not commit real credentials.

The Android app currently communicates over BLE; Wi-Fi/server connectivity is not part of the app yet.

### Parts List

The following parts list is based on the project BOM in [PartsList.pdf](PartsList.pdf).

| Item | Cost | Link |
| --- | ---: | --- |
| ESP32 board | 850.00 | MD0929 - SuperMini ESP32-C3 Dev Board Type-C WiFi Bluetooth IoT |
| Charging Module | 350.00 | Mini Solar Battery Charging Module LiPo CN3065 Input 4.4-6V Out 500mA (MD0397) Products |
| Solar Panel | 280.00 | BA0146 - Mini Solar Panel 5V 200mA 99x69mm |
| LiPo Boost | 150.00 | MD0745 - Multi-function Mini Boost Module Step Up Board 5/8/9/12V 1.5A |
| Soil moisture | 330.00 | Capacitive Soil Moisture Sensor V2.0 (MD0751) Products |
| Lipo Battery | 530.00 | 3.7V 250mAh Lipo Battery (BA0149) Products |
| Light sensor | 350.00 | MD0249 - TEMT6000 Professional Light Sensor Module |
| Total Cost | 2,840.00 | - |

## Status

Version 1 has an ESP32 BLE firmware sketch and an Android app that receives sensor data and supports threshold configuration. Version 2 has early Wi-Fi, NTP time synchronization, and LittleFS logging code; sending data to a server is the next implementation step. Sensor calibration, hardware validation, and field testing remain outstanding.

## Team

MorAura team project.
