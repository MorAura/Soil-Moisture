package com.moraura.soilapp.ble

import java.util.UUID

object SoilSenseConstants {
    const val DEVICE_NAME = "SoilSense"
    val SERVICE_UUID: UUID = UUID.fromString("0d8d71f3-afea-4903-a389-d2db0912c1b1")

    // Notifications
    val MOIST_CHAR_UUID: UUID = UUID.fromString("48ea775b-a582-4b00-b1b9-cf789c55887f")
    val TEMP_CHAR_UUID: UUID = UUID.fromString("2b2c8a8b-25cd-4804-a2de-b3a27c64758c")
    val LIGHT_CHAR_UUID: UUID = UUID.fromString("18ad5c89-a11b-4a1e-b655-6ee4c820ce4a")

    // Thresholds
    val MOIST_MAX_CHAR_UUID: UUID = UUID.fromString("4711d9f9-c38e-4f0f-9ef9-4e89e1f88391")
    val MOIST_MIN_CHAR_UUID: UUID = UUID.fromString("7470e5d5-00ee-49a4-87ba-88ad813b287b")
    val TEMP_MAX_CHAR_UUID: UUID = UUID.fromString("5f59d498-e642-426f-b63f-3e9dd58465f3")
    val TEMP_MIN_CHAR_UUID: UUID = UUID.fromString("014930eb-902d-4fbc-b7be-1f85da76fa37")

    // WiFi Configuration
    val ALERT_SSID_UUID: UUID = UUID.fromString("d93c1417-f814-41c6-b38c-6d145b4df240")
    val ALERT_PASS_UUID: UUID = UUID.fromString("74d2f3b5-9553-409f-81d2-b2670feecaec")

    const val EXPECTED_DATA_POINTS = 168
    const val REQUESTED_MTU = 517
}
