package com.example.soilapp.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.example.soilapp.data.model.ConnectionState
import com.example.soilapp.data.model.SoilSenseMetrics
import com.example.soilapp.data.model.SoilSenseThresholds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import kotlin.time.Duration.Companion.milliseconds

private const val TAG = "BleManager"

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var hasInitializedGatt = false

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState = _connectionState.asStateFlow()

    private val _foundDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val foundDevices = _foundDevices.asStateFlow()

    private val _metrics = MutableStateFlow(SoilSenseMetrics())
    val metrics = _metrics.asStateFlow()

    private val _thresholds = MutableStateFlow(SoilSenseThresholds())
    val thresholds = _thresholds.asStateFlow()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device !in _foundDevices.value) {
                _foundDevices.update { it + device }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed: $errorCode")
            _connectionState.value = ConnectionState.Error("Scan failed: $errorCode")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        Log.d(TAG, "Connected to GATT server")
                        _connectionState.value = ConnectionState.Connected
                        gatt.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Log.d(TAG, "Disconnected from GATT server")
                        _connectionState.value = ConnectionState.Disconnected
                        bluetoothGatt = null
                        hasInitializedGatt = false
                    }
                }
            } else {
                Log.e(TAG, "GATT error: $status")
                _connectionState.value = ConnectionState.Error("GATT error: $status")
                gatt.close()
                bluetoothGatt = null
                hasInitializedGatt = false
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")
                hasInitializedGatt = false
                val mtuRequested = gatt.requestMtu(SoilSenseConstants.REQUESTED_MTU)
                Log.d(TAG, "requestMtu returned $mtuRequested")
                if (!mtuRequested) {
                    enableNotificationsAndReadThresholds(gatt)
                } else {
                    // Fallback in case onMtuChanged is not invoked by system
                    scope.launch {
                        delay(1000.milliseconds)
                        if (!hasInitializedGatt) {
                            Log.d(TAG, "MTU change callback timed out, initializing notifications now")
                            enableNotificationsAndReadThresholds(gatt)
                        }
                    }
                }
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Log.d(TAG, "onMtuChanged: mtu=$mtu, status=$status")
            enableNotificationsAndReadThresholds(gatt)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            handleCharacteristicUpdate(characteristic)
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            handleCharacteristicUpdate(characteristic, value)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleCharacteristicUpdate(characteristic)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                handleCharacteristicUpdate(characteristic, value)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Characteristic write success: ${characteristic.uuid}")
                handleCharacteristicUpdate(characteristic)
            }
        }
    }

    fun startScan() {
        _foundDevices.value = emptyList()
        scope.launch(Dispatchers.IO) {
            val adapter = bluetoothAdapter
            val scanner = adapter?.bluetoothLeScanner
            if (scanner == null) {
                _connectionState.value = ConnectionState.Error("Bluetooth not supported")
                return@launch
            }

            _connectionState.value = ConnectionState.Scanning
            val filter = ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SoilSenseConstants.SERVICE_UUID))
                .build()
            val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build()

            scanner.startScan(listOf(filter), settings, scanCallback)
        }
    }

    fun stopScan() {
        scope.launch(Dispatchers.IO) {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    fun connect(device: BluetoothDevice) {
        scope.launch(Dispatchers.IO) {
            hasInitializedGatt = false
            _metrics.value = SoilSenseMetrics()
            _thresholds.value = SoilSenseThresholds()
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            _connectionState.value = ConnectionState.Connecting
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        }
    }

    fun disconnect() {
        scope.launch(Dispatchers.IO) {
            hasInitializedGatt = false
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
        }
    }

    private fun enableNotificationsAndReadThresholds(gatt: BluetoothGatt) {
        if (hasInitializedGatt) return
        hasInitializedGatt = true

        val service = gatt.getService(SoilSenseConstants.SERVICE_UUID) ?: run {
            Log.e(TAG, "SoilSense service not found!")
            return
        }
        
        scope.launch {
            val notificationChars = listOf(
                SoilSenseConstants.MOIST_CHAR_UUID,
                SoilSenseConstants.TEMP_CHAR_UUID,
                SoilSenseConstants.LIGHT_CHAR_UUID
            )
            // 1. Enable notifications
            for (uuid in notificationChars) {
                service.getCharacteristic(uuid)?.let { characteristic ->
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    descriptor?.let {
                        gatt.writeDescriptor(it, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        Log.d(TAG, "Write descriptor for notification $uuid")
                        delay(300.milliseconds)
                    }
                }
            }

            // 2. Read initial live metric values directly
            for (uuid in notificationChars) {
                service.getCharacteristic(uuid)?.let { characteristic ->
                    val success = gatt.readCharacteristic(characteristic)
                    Log.d(TAG, "Read metric characteristic $uuid success: $success")
                    delay(250.milliseconds)
                }
            }

            // 3. Read threshold values
            val thresholdChars = listOf(
                SoilSenseConstants.MOIST_MAX_CHAR_UUID,
                SoilSenseConstants.MOIST_MIN_CHAR_UUID,
                SoilSenseConstants.TEMP_MAX_CHAR_UUID,
                SoilSenseConstants.TEMP_MIN_CHAR_UUID
            )
            for (uuid in thresholdChars) {
                service.getCharacteristic(uuid)?.let { characteristic ->
                    val success = gatt.readCharacteristic(characteristic)
                    Log.d(TAG, "Read threshold characteristic $uuid success: $success")
                    delay(250.milliseconds)
                }
            }
        }
    }

    private fun handleCharacteristicUpdate(characteristic: BluetoothGattCharacteristic, value: ByteArray? = null) {
        @Suppress("DEPRECATION")
        val data = value ?: characteristic.value ?: return
        Log.d(TAG, "Received characteristic update: ${characteristic.uuid}: ${data.contentToString()}")

        val parsedList = parseByteArray(data)
        if (parsedList.isEmpty()) return

        when (characteristic.uuid) {
            SoilSenseConstants.MOIST_CHAR_UUID -> {
                _metrics.update { current ->
                    current.copy(moisture = if (current.moisture.isEmpty()) parsedList else current.moisture + parsedList)
                }
            }
            SoilSenseConstants.TEMP_CHAR_UUID -> {
                _metrics.update { current ->
                    current.copy(temperature = if (current.temperature.isEmpty()) parsedList else current.temperature + parsedList)
                }
            }
            SoilSenseConstants.LIGHT_CHAR_UUID -> {
                _metrics.update { current ->
                    current.copy(light = if (current.light.isEmpty()) parsedList else current.light + parsedList)
                }
            }
            SoilSenseConstants.MOIST_MAX_CHAR_UUID -> {
                _thresholds.update { it.copy(moistureMax = data[0].toInt() and 0xFF) }
            }
            SoilSenseConstants.MOIST_MIN_CHAR_UUID -> {
                _thresholds.update { it.copy(moistureMin = data[0].toInt() and 0xFF) }
            }
            SoilSenseConstants.TEMP_MAX_CHAR_UUID -> {
                _thresholds.update { it.copy(temperatureMax = data[0].toInt() and 0xFF) }
            }
            SoilSenseConstants.TEMP_MIN_CHAR_UUID -> {
                _thresholds.update { it.copy(temperatureMin = data[0].toInt() and 0xFF) }
            }
        }
    }

    private fun parseByteArray(data: ByteArray): List<Int> {
        return data.map { it.toInt() and 0xFF }
    }

    fun writeThreshold(uuid: UUID, value: Int) {
        updateLocalThreshold(uuid, value)
        scope.launch(Dispatchers.IO) {
            val gatt = bluetoothGatt ?: return@launch
            val service = gatt.getService(SoilSenseConstants.SERVICE_UUID) ?: return@launch
            val characteristic = service.getCharacteristic(uuid) ?: return@launch
            
            @Suppress("DEPRECATION")
            characteristic.value = byteArrayOf(value.toByte())
            @Suppress("DEPRECATION")
            gatt.writeCharacteristic(characteristic)
        }
    }

    // update locally stored threshold values
    private fun updateLocalThreshold(uuid: UUID, value: Int) {
        when (uuid) {
            SoilSenseConstants.MOIST_MAX_CHAR_UUID -> _thresholds.update { it.copy(moistureMax = value) }
            SoilSenseConstants.MOIST_MIN_CHAR_UUID -> _thresholds.update { it.copy(moistureMin = value) }
            SoilSenseConstants.TEMP_MAX_CHAR_UUID -> _thresholds.update { it.copy(temperatureMax = value) }
            SoilSenseConstants.TEMP_MIN_CHAR_UUID -> _thresholds.update { it.copy(temperatureMin = value) }
        }
    }
}
