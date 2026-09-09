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

The prototype uses an ESP32 to collect readings from environmental sensors and share them with a simple mobile app over Bluetooth Low Energy (BLE) or Wi-Fi.
There are pros and cons to each communication method, and the final implementation has not been decided yet.

```text
Light intensity sensor ─┐
Temperature sensor     ─┼──> ESP32 ──Wi-Fi / BLE ──> Mobile app
Soil moisture sensor   ─┘             |      		 	│
									  |				 	├──> Adjust monitoring ranges
									  |					|
									  └──> Webpage (Captive Portal)
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
├── ProjectProposal.pdf
└── Sensor_Code/
	└── Sensor_Code.ino

```

## Getting Started

### Prerequisites

- ESP32 development board
- Soil moisture sensor
- Light intensity sensor
- Temperature sensor
- USB cable and a compatible ESP32 development environment

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

### Firmware

Firmware is still under development. Add the ESP32 source files to [`Sensor_Code/`](Sensor_Code/) and document the required board configuration, libraries, and wiring here.

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
