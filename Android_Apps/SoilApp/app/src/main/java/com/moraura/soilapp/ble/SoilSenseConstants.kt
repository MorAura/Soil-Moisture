package com.moraura.soilapp.ble

import java.util.UUID

object SoilSenseConstants {
    const val DEVICE_NAME = "SoilSense"
    val SERVICE_UUID: UUID = UUID.fromString("0d8d71f3-afea-4903-a389-d2db0912c1b1")

    // Notifications
    val MOIST_CHAR_UUID: UUID = UUID.fromString("48ea775b-a582-4b00-b1b9-cf789c55887f")
    val TEMP_CHAR_UUID: UUID = UUID.fromString("2b2c8a8b-25cd-4804-a2de-b3a27c64758c")
    val LIGHT_CHAR_UUID: UUID = UUID.fromString("18ad5c89-a11b-4a1e-b655-6ee4c820ce4a")

    // Realtime Notifications
    val MOISTRT_CHAR_UUID: UUID = UUID.fromString("a6043c33-8c01-484a-aad4-f26c80b73e4f")
    val TEMPRT_CHAR_UUID: UUID = UUID.fromString("f0b4ca2b-4510-4253-afac-0cb8582f5bc4")
    val LIGHTRT_CHAR_UUID: UUID = UUID.fromString("fb7437e8-4283-4583-82c8-87dce40b8876")

    // Thresholds
    val MOIST_MAX_CHAR_UUID: UUID = UUID.fromString("4711d9f9-c38e-4f0f-9ef9-4e89e1f88391")
    val MOIST_MIN_CHAR_UUID: UUID = UUID.fromString("7470e5d5-00ee-49a4-87ba-88ad813b287b")
    val TEMP_MAX_CHAR_UUID: UUID = UUID.fromString("5f59d498-e642-426f-b63f-3e9dd58465f3")
    val TEMP_MIN_CHAR_UUID: UUID = UUID.fromString("014930eb-902d-4fbc-b7be-1f85da76fa37")

    // WiFi Configuration
    val ALERT_SSID_UUID: UUID = UUID.fromString("d93c1417-f814-41c6-b38c-6d145b4df240")
    val ALERT_PASS_UUID: UUID = UUID.fromString("74d2f3b5-9553-409f-81d2-b2670feecaec")

    val DEVICE_NAME_UUID: UUID = UUID.fromString("47bd94ad-57af-4b11-964f-97a3bf94b396")

    val DEVICE_ID_UUID: UUID = UUID.fromString("03034c39-411a-40ec-b480-2c69edc542b4")

    const val EXPECTED_DATA_POINTS = 168
    const val REQUESTED_MTU = 517
}
