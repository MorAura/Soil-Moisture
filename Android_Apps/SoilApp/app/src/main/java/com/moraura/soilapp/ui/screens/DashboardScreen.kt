package com.moraura.soilapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moraura.soilapp.data.model.ConnectionState
import com.moraura.soilapp.data.model.SoilSenseMetrics
import com.moraura.soilapp.data.model.SoilSenseThresholds
import com.moraura.soilapp.ui.SoilSenseViewModel
import com.moraura.soilapp.ui.components.TopGradientScreen
import com.moraura.soilapp.ui.theme.SoilAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: SoilSenseViewModel,
    onNavigateBack: () -> Unit
) {
    val metrics by viewModel.metrics.collectAsState()
    val thresholds by viewModel.thresholds.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()

    BackHandler {
        viewModel.disconnect()
        onNavigateBack()
    }

    DashboardContent(
        metrics = metrics,
        thresholds = thresholds,
        connectionState = connectionState,
        onDisconnect = { 
            viewModel.disconnect()
        },
        onNavigateBack = {
            viewModel.disconnect()
            onNavigateBack()
        },
        onUpdateMoistureMin = { viewModel.updateMoistureMin(it) },
        onUpdateMoistureMax = { viewModel.updateMoistureMax(it) },
        onUpdateTemperatureMin = { viewModel.updateTemperatureMin(it) },
        onUpdateTemperatureMax = { viewModel.updateTemperatureMax(it) },
        onUpdateWifiCredentials = { ssid, password ->
            viewModel.updateWifiCredentials(ssid, password)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    metrics: SoilSenseMetrics,
    thresholds: SoilSenseThresholds,
    connectionState: ConnectionState,
    onDisconnect: () -> Unit,
    onUpdateMoistureMin: (Int) -> Unit,
    onUpdateMoistureMax: (Int) -> Unit,
    onUpdateTemperatureMin: (Int) -> Unit,
    onUpdateTemperatureMax: (Int) -> Unit,
    onUpdateWifiCredentials: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Live", "History", "Settings") //

    TopGradientScreen {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    expandedHeight = 80.dp,
                    title = {
                        Text(
                            text = "Dashboard",
                            fontWeight = FontWeight.Medium,
                        )
                    },
                    navigationIcon = {
                        FilledTonalIconButton(
                            modifier = Modifier
                                .padding(8.dp)   // distance from left edge
                                .size(48.dp),
                            onClick = onNavigateBack,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(0.8f),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
//                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    actions = {
                        if (connectionState == ConnectionState.Connected) {
                            TextButton(onClick = onDisconnect, modifier = Modifier.padding(end = 8.dp)) {
                                Text("Disconnect", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "SoilSense Plant",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ConnectionStatusCard(connectionState)
                }


                Spacer(modifier = Modifier.height(16.dp))

                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

//                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
                    when (selectedTabIndex) {
                        0 -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "Live Metrics",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 150.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    item {
                                        val lastMoisture = metrics.moisture.lastOrNull()
                                        val isOutOfRange =
                                            lastMoisture != null && (lastMoisture < thresholds.moistureMin || lastMoisture > thresholds.moistureMax)
                                        MetricCard(
                                            title = "Moisture",
                                            value = lastMoisture?.toString() ?: "--",
                                            unit = "%",
                                            icon = Icons.Rounded.WaterDrop,
                                            isAlert = isOutOfRange
                                        )
                                    }
                                    item {
                                        val lastTemp = metrics.temperature.lastOrNull()
                                        val isOutOfRange =
                                            lastTemp != null && (lastTemp < thresholds.temperatureMin || lastTemp > thresholds.temperatureMax)
                                        MetricCard(
                                            title = "Temperature",
                                            value = lastTemp?.toString() ?: "--",
                                            unit = "°C",
                                            icon = Icons.Rounded.Thermostat,
                                            isAlert = isOutOfRange
                                        )
                                    }
                                    item {
                                        MetricCard(
                                            title = "Light Level",
                                            value = metrics.light.lastOrNull()?.toString() ?: "--",
                                            unit = "lux",
                                            icon = Icons.Rounded.LightMode,
                                            isAlert = false // Light doesn't have thresholds
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "History",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                HistoryContent(
                                    metrics = metrics,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        2 -> {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = "Thresholds",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                ThresholdsContent(
                                    thresholds = thresholds,
                                    onUpdateMoistureMin = { onUpdateMoistureMin(it) },
                                    onUpdateMoistureMax = { onUpdateMoistureMax(it) },
                                    onUpdateTemperatureMin = { onUpdateTemperatureMin(it) },
                                    onUpdateTemperatureMax = { onUpdateTemperatureMax(it) },
                                    onUpdateWifiCredentials = { ssid, password ->
                                        onUpdateWifiCredentials(ssid, password)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(state: ConnectionState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (state) {
                is ConnectionState.Connected -> MaterialTheme.colorScheme.primaryContainer
                is ConnectionState.Error -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusText = when (state) {
                ConnectionState.Connected -> "Connected"
                ConnectionState.Disconnected -> "Disconnected"
                ConnectionState.Connecting -> "Connecting..."
                ConnectionState.Scanning -> "Scanning..."
                is ConnectionState.Error -> "Error: ${state.message}"
            }
            val statusIcon = when (state) {
                ConnectionState.Connected -> Icons.Rounded.CloudDone
                is ConnectionState.Error -> Icons.Rounded.Error
                else -> Icons.AutoMirrored.Rounded.BluetoothSearching
            }

            Icon(statusIcon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    isAlert: Boolean
) {
    val color = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val containerColor = if (isAlert) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.labelLarge, color = color)
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.titleMedium,
                    color = color.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DashboardScreenPreview() {
    SoilAppTheme {
        DashboardContent(
            metrics = SoilSenseMetrics(
                moisture = listOf(45),
                temperature = listOf(32),
                light = listOf(85),
                realTimeMoisture = 45,
                realTimeTemperature = 32,
                realTimeLight = 85
            ),
            thresholds = SoilSenseThresholds(
                moistureMin = 30,
                moistureMax = 70,
                temperatureMin = 15,
                temperatureMax = 30
            ),
            connectionState = ConnectionState.Connected,
            onDisconnect = {},
            onUpdateMoistureMin = {},
            onUpdateMoistureMax = {},
            onUpdateTemperatureMin = {},
            onUpdateTemperatureMax = {},
            onUpdateWifiCredentials = { _, _ -> },
            onNavigateBack = {},
        )
    }
}
