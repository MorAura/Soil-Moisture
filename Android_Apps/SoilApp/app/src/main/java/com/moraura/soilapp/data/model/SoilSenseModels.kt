package com.moraura.soilapp.data.model

data class SoilSenseMetrics(
    val moisture: List<Int> = emptyList(),
    val temperature: List<Int> = emptyList(),
    val light: List<Int> = emptyList(),
    val realTimeMoisture: Int? = null,
    val realTimeTemperature: Int? = null,
    val realTimeLight: Int? = null,
)

data class SoilSenseThresholds(
    val moistureMax: Int = 0,
    val moistureMin: Int = 0,
    val temperatureMax: Int = 0,
    val temperatureMin: Int = 0,
    val wifiSsid: String = "",
    val wifiPassword: String = "",
    val deviceName: String = ""
)

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Scanning : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
