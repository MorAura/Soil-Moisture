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

private const val TAG = "BleManager"

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private val scope = CoroutineScope(Dispatchers.Main)

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
                    }
                }
            } else {
                Log.e(TAG, "GATT error: $status")
                _connectionState.value = ConnectionState.Error("GATT error: $status")
                gatt.close()
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered")
                gatt.requestMtu(SoilSenseConstants.REQUESTED_MTU)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "MTU changed to $mtu")
                enableNotificationsAndReadThresholds(gatt)
            }
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
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            _connectionState.value = ConnectionState.Connecting
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
        }
    }

    fun disconnect() {
        scope.launch(Dispatchers.IO) {
            bluetoothGatt?.disconnect()
        }
    }

    private fun enableNotificationsAndReadThresholds(gatt: BluetoothGatt) {
        val service = gatt.getService(SoilSenseConstants.SERVICE_UUID) ?: return
        
        scope.launch {
            val notificationChars = listOf(
                SoilSenseConstants.MOIST_CHAR_UUID,
                SoilSenseConstants.TEMP_CHAR_UUID,
                SoilSenseConstants.LIGHT_CHAR_UUID
            )
            for (uuid in notificationChars) {
                service.getCharacteristic(uuid)?.let { characteristic ->
                    gatt.setCharacteristicNotification(characteristic, true)
                    val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                    descriptor?.let {
                        it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        val success = gatt.writeDescriptor(it)
                        Log.d(TAG, "Write descriptor for notification $uuid success: $success")
                        delay(300) // Properly queued and delayed to prevent concurrent write failures
                    }
                }
            }

            val thresholdChars = listOf(
                SoilSenseConstants.MOIST_MAX_CHAR_UUID,
                SoilSenseConstants.MOIST_MIN_CHAR_UUID,
                SoilSenseConstants.TEMP_MAX_CHAR_UUID,
                SoilSenseConstants.TEMP_MIN_CHAR_UUID
            )
            for (uuid in thresholdChars) {
                service.getCharacteristic(uuid)?.let { characteristic ->
                    val success = gatt.readCharacteristic(characteristic)
                    Log.d(TAG, "Read characteristic $uuid success: $success")
                    delay(250) // Properly queued and delayed between reads
                }
            }
        }
    }

    private fun handleCharacteristicUpdate(characteristic: BluetoothGattCharacteristic, value: ByteArray? = null) {
        val data = value ?: characteristic.value ?: return
        Log.d(TAG, "Received characteristic update: ${characteristic.uuid}: ${data.contentToString()}")

        when (characteristic.uuid) {
            SoilSenseConstants.MOIST_CHAR_UUID -> {
                _metrics.update { it.copy(moisture = parseByteArray(data)) }
            }
            SoilSenseConstants.TEMP_CHAR_UUID -> {
                _metrics.update { it.copy(temperature = parseByteArray(data)) }
            }
            SoilSenseConstants.LIGHT_CHAR_UUID -> {
                _metrics.update { it.copy(light = parseByteArray(data)) }
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
        scope.launch(Dispatchers.IO) {
            val gatt = bluetoothGatt ?: return@launch
            val service = gatt.getService(SoilSenseConstants.SERVICE_UUID) ?: return@launch
            val characteristic = service.getCharacteristic(uuid) ?: return@launch
            
            characteristic.value = byteArrayOf(value.toByte())
            gatt.writeCharacteristic(characteristic)
        }
    }
}
