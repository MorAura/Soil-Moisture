# Smartsoil Condition Monitoring

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

The prototype uses an ESP32 to collect readings from environmental sensors and share them with a simple mobile app over Bluetooth Low Energy (BLE).

```text
Soil moisture sensor  ─┐
Light intensity sensor ─┤
Temperature sensor     ─┼──> ESP32 ──BLE──> Mobile app
Humidity sensor        ─┘                    │
											 └──> Adjust monitoring ranges
```

### Sensors

- Soil moisture
- Light intensity
- Temperature
- Humidity

## Example Monitoring Profile

The following values are illustrative targets for the prototype. They should be calibrated for the plant species and real growing environment.

| Condition | Example target range |
| --- | --- |
| Soil moisture | 40–65% |
| Light | 1,000–10,000 lux |
| Temperature | 20–28°C |
| Humidity | 60–80% |

When a reading leaves its target range, the system should notify the user that the condition is out of range.

## Build and Field Test Plan

1. Research the problem and suitable sensors.
2. Assemble an ESP32 prototype.
3. Visit plantations to survey users and collect feedback.
4. Calibrate the monitoring ranges and improve the prototype based on feedback.
5. Test the system at real plantation sites.

The field visits will help confirm that the system is practical for real-world scenarios.

## Repository Structure

```text
.
├── README.md
└── Sensor_Code/
	└── TODO: add ESP32 firmware
```

## Getting Started

### Prerequisites

- ESP32 development board
- Soil moisture sensor
- Light intensity sensor
- Temperature and humidity sensor
- USB cable and a compatible ESP32 development environment

### Firmware

Firmware is not yet included. Add the ESP32 source files to [`Sensor_Code/`](Sensor_Code/) and document the required board configuration, libraries, and wiring here.

```text
TODO:
- Add sensor wiring details.
- Add the ESP32 firmware.
- Add BLE service and characteristic definitions.
- Add setup, flashing, and calibration instructions.
```

## Status

This repository currently contains the project proposal and initial documentation. Hardware assembly, firmware, mobile app integration, and field testing are planned next.

## Team

MorAura team project.
