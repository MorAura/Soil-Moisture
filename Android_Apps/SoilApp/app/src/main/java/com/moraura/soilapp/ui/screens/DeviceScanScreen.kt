package com.moraura.soilapp.ui.screens

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moraura.soilapp.ble.SoilSenseConstants
import com.moraura.soilapp.data.model.ConnectionState
import com.moraura.soilapp.ui.SoilSenseViewModel
import com.moraura.soilapp.ui.theme.SoilAppTheme

data class DiscoveredDevice(
    val name: String,
    val address: String,
    val rawDevice: BluetoothDevice? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun DeviceScanScreen(
    viewModel: SoilSenseViewModel,
    onDeviceSelected: (BluetoothDevice) -> Unit
) {
    val foundDevices by viewModel.foundDevices.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    var filterSoilSense by remember { mutableStateOf(false) }

    val filteredDevices = remember(foundDevices, filterSoilSense) {
        val list = if (filterSoilSense) {
            foundDevices.filter { it.name?.contains(SoilSenseConstants.DEVICE_NAME, ignoreCase = true) == true }
        } else {
            foundDevices
        }
        list.map { device ->
            DiscoveredDevice(
                name = device.name ?: "Unknown Device",
                address = device.address,
                rawDevice = device
            )
        }
    }

    DeviceScanContent(
        devices = filteredDevices,
        connectionState = connectionState,
        filterSoilSense = filterSoilSense,
        onFilterToggle = { filterSoilSense = !filterSoilSense },
        onStartScan = { viewModel.startScanning() },
        onStopScan = { viewModel.stopScanning() },
        onDeviceClick = { discovered ->
            discovered.rawDevice?.let { onDeviceSelected(it) }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.startScanning()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScanContent(
    devices: List<DiscoveredDevice>,
    connectionState: ConnectionState,
    filterSoilSense: Boolean,
    onFilterToggle: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (DiscoveredDevice) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "MorAura",
                                fontWeight = FontWeight.Bold,
                                )
//                            Text(
//                                "Soil Monitor",
//                                style = MaterialTheme.typography.labelMedium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                            Text(
//                                "Device Discovery",
//                                style = MaterialTheme.typography.labelMedium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onFilterToggle) {
                            Icon(
                                imageVector = Icons.Rounded.FilterList,
                                contentDescription = "Show only named SoilSense devices",
                                tint = if (filterSoilSense) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (connectionState == ConnectionState.Scanning) {
                            IconButton(onClick = onStopScan) {
                                Icon(Icons.Rounded.Close, contentDescription = "Stop Scanning")
                            }
                        } else {
                            IconButton(onClick = onStartScan) {
                                Icon(Icons.Rounded.Refresh, contentDescription = "Scan Again")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (connectionState is ConnectionState.Scanning) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        trackColor = MaterialTheme.colorScheme.primaryContainer
                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = "Searching for SoilSense sensors nearby...",
//                        style = MaterialTheme.typography.labelSmall,
//                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
//                        color = MaterialTheme.colorScheme.primary,
//                    )
                }

                if (devices.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (connectionState is ConnectionState.Scanning) {
                                Text(
                                    text = if (filterSoilSense) "No devices matching 'SoilSense' found" else "Searching for SoilSense devices...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Text(
                                    text = if (filterSoilSense) "No devices matching 'SoilSense' found" else "No devices found",
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            if (!filterSoilSense && connectionState !is ConnectionState.Scanning) {
                                Button(
                                    onClick = onStartScan,
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text("Scan Again")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(devices) { device ->
                            DeviceItem(
                                name = device.name,
                                address = device.address,
                                onClick = { onDeviceClick(device) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceItem(
    name: String,
    address: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Bluetooth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DeviceScanScreenPreview() {
    SoilAppTheme {
        DeviceScanContent(
            devices = listOf(
                DiscoveredDevice("SoilSense Sensor #1", "00:11:22:33:44:55"),
                DiscoveredDevice("SoilSense Sensor #2", "AA:BB:CC:DD:EE:FF")
            ),
            connectionState = ConnectionState.Scanning,
            filterSoilSense = false,
            onFilterToggle = {},
            onStartScan = {},
            onStopScan = {},
            onDeviceClick = {}
        )
    }
}
