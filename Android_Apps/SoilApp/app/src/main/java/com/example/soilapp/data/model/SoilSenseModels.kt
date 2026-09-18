package com.example.soilapp.data.model

data class SoilSenseMetrics(
    val moisture: List<Int> = emptyList(),
    val temperature: List<Int> = emptyList(),
    val light: List<Int> = emptyList()
)

data class SoilSenseThresholds(
    val moistureMax: Int = 0,
    val moistureMin: Int = 0,
    val temperatureMax: Int = 0,
    val temperatureMin: Int = 0
)

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Scanning : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
