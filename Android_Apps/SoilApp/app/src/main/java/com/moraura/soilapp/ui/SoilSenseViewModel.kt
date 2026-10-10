package com.moraura.soilapp.ui

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import com.moraura.soilapp.ble.BleManager
import com.moraura.soilapp.ble.SoilSenseConstants
import java.util.UUID

class SoilSenseViewModel(application: Application) : AndroidViewModel(application) {
    private val bleManager = BleManager(application)

    val connectionState = bleManager.connectionState
    val foundDevices = bleManager.foundDevices
    val metrics = bleManager.metrics
    val thresholds = bleManager.thresholds

    fun startScanning() {
        bleManager.startScan()
    }

    fun stopScanning() {
        bleManager.stopScan()
    }

    fun connect(device: BluetoothDevice) {
        bleManager.connect(device)
    }

    fun disconnect() {
        bleManager.disconnect()
    }

    fun updateThreshold(uuid: UUID, value: Int) {
        bleManager.writeThreshold(uuid, value)
    }

    fun updateMoistureMax(value: Int) = updateThreshold(SoilSenseConstants.MOIST_MAX_CHAR_UUID, value)
    fun updateMoistureMin(value: Int) = updateThreshold(SoilSenseConstants.MOIST_MIN_CHAR_UUID, value)
    fun updateTemperatureMax(value: Int) = updateThreshold(SoilSenseConstants.TEMP_MAX_CHAR_UUID, value)
    fun updateTemperatureMin(value: Int) = updateThreshold(SoilSenseConstants.TEMP_MIN_CHAR_UUID, value)

    fun updateWifiSsid(value: String) {
        bleManager.writeStringCharacteristic(SoilSenseConstants.ALERT_SSID_UUID, value)
    }

    fun updateWifiPassword(value: String) {
        bleManager.writeStringCharacteristic(SoilSenseConstants.ALERT_PASS_UUID, value)
    }

    fun updateWifiCredentials(ssid: String, pass: String) {
        updateWifiSsid(ssid)
        updateWifiPassword(pass)
    }
}
