package com.moraura.soilapp.ui

import android.app.Application
import android.bluetooth.BluetoothDevice
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moraura.soilapp.ble.BleManager
import com.moraura.soilapp.ble.SoilSenseConstants
import com.moraura.soilapp.data.local.AlertTopicEntity
import com.moraura.soilapp.data.repository.AlertTopicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

class SoilSenseViewModel(application: Application) : AndroidViewModel(application) {
    private val bleManager = BleManager(application)
    private val alertTopicRepository = AlertTopicRepository(application)

    val connectionState = bleManager.connectionState
    val foundDevices = bleManager.foundDevices
    val metrics = bleManager.metrics
    val thresholds = bleManager.thresholds
    val alertTopics: Flow<List<AlertTopicEntity>> = alertTopicRepository.allAlertTopics

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

    fun updateDeviceName(value: String) {
        bleManager.writeStringCharacteristic(SoilSenseConstants.DEVICE_NAME_UUID, value)
    }

    fun updateWifiCredentials(ssid: String, pass: String) {
        viewModelScope.launch {
            updateWifiSsid(ssid)
            delay(200.milliseconds)
            updateWifiPassword(pass)
        }
    }

    fun saveAlertTopic(deviceId: String, deviceName: String, alertUrl: String) {
        viewModelScope.launch {
            alertTopicRepository.saveAlertTopic(deviceId, deviceName, alertUrl)
        }
    }

    fun deleteAlertTopic(deviceId: String) {
        viewModelScope.launch {
            alertTopicRepository.deleteAlertTopic(deviceId)
        }
    }
}
