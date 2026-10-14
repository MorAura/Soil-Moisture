package com.moraura.soilapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moraura.soilapp.data.model.SoilSenseThresholds
import com.moraura.soilapp.ui.SoilSenseViewModel
import com.moraura.soilapp.ui.theme.SoilAppTheme
import kotlin.math.roundToInt

@Composable
fun ThresholdsScreen(
    viewModel: SoilSenseViewModel,
    onNavigateBack: () -> Unit
) {
    val thresholds by viewModel.thresholds.collectAsState()

    ThresholdsContent(
        thresholds = thresholds,
        onUpdateMoistureMin = { viewModel.updateMoistureMin(it) },
        onUpdateMoistureMax = { viewModel.updateMoistureMax(it) },
        onUpdateTemperatureMin = { viewModel.updateTemperatureMin(it) },
        onUpdateTemperatureMax = { viewModel.updateTemperatureMax(it) },
        onUpdateWifiCredentials = { ssid, password ->
            viewModel.updateWifiCredentials(ssid, password)
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThresholdsContent(
    thresholds: SoilSenseThresholds,
    onUpdateMoistureMin: (Int) -> Unit,
    onUpdateMoistureMax: (Int) -> Unit,
    onUpdateTemperatureMin: (Int) -> Unit,
    onUpdateTemperatureMax: (Int) -> Unit,
    onUpdateWifiCredentials: (ssid: String, password: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Configuration") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ThresholdCategory(
                title = "Moisture Thresholds",
                icon = Icons.Rounded.WaterDrop,
                min = thresholds.moistureMin,
                max = thresholds.moistureMax,
                range = 0f..100f,
                unit = "%",
                onMinChange = onUpdateMoistureMin,
                onMaxChange = onUpdateMoistureMax
            )

            ThresholdCategory(
                title = "Temperature Thresholds",
                icon = Icons.Rounded.Thermostat,
                min = thresholds.temperatureMin,
                max = thresholds.temperatureMax,
                range = 0f..50f,
                unit = "°C",
                onMinChange = onUpdateTemperatureMin,
                onMaxChange = onUpdateTemperatureMax
            )

            DeviceNameCategory(
                deviceName = thresholds.deviceName
            )

            WifiConfigCategory(
                ssid = thresholds.wifiSsid,
                password = thresholds.wifiPassword,
                onSaveWifi = onUpdateWifiCredentials
            )
        }
    }
}

@Composable
fun DeviceNameCategory(
    deviceName: String
) {
    var nameInput by remember(deviceName) { mutableStateOf(deviceName) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Computer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Device Setup",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Device Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { },
                modifier = Modifier.align(Alignment.End)
            ) {
//                Icon(Icons.Rounded.Save, contentDescription = null)
//                Spacer(modifier = Modifier.width(8.dp))
                Text("Update")
            }
        }
    }
}

@Composable
fun WifiConfigCategory(
    ssid: String,
    password: String,
    onSaveWifi: (ssid: String, password: String) -> Unit
) {
    var ssidInput by remember(ssid) { mutableStateOf(ssid) }
    var passwordInput by remember(password) { mutableStateOf(password) }
    var passwordVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "WiFi Credentials", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = ssidInput,
                onValueChange = { ssidInput = it },
                label = { Text("WiFi SSID") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { passwordInput = it },
                label = { Text("WiFi Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSaveWifi(ssidInput, passwordInput) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Rounded.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save WiFi Credentials")
            }
        }
    }
}

@Composable
fun ThresholdCategory(
    title: String,
    icon: ImageVector,
    min: Int,
    max: Int,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onMinChange: (Int) -> Unit,
    onMaxChange: (Int) -> Unit
) {
    var localMin by remember(min) { mutableFloatStateOf(min.toFloat()) }
    var localMax by remember(max) { mutableFloatStateOf(max.toFloat()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Minimum Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Minimum $unit", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${localMin.roundToInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Slider(
                    value = localMin,
                    onValueChange = { newValue ->
                        localMin = newValue.coerceAtMost(localMax)
                    },
                    onValueChangeFinished = { onMinChange(localMin.roundToInt()) },
                    valueRange = range,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Maximum Slider
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Maximum $unit", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${localMax.roundToInt()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Slider(
                    value = localMax,
                    onValueChange = { newValue ->
                        localMax = newValue.coerceAtLeast(localMin)
                    },
                    onValueChangeFinished = { onMaxChange(localMax.roundToInt()) },
                    valueRange = range,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ThresholdsScreenPreview() {
    SoilAppTheme {
        ThresholdsContent(
            thresholds = SoilSenseThresholds(
                moistureMin = 30,
                moistureMax = 70,
                temperatureMin = 15,
                temperatureMax = 35,
                wifiSsid = "MyHomeWiFi",
                wifiPassword = "Password123",
                deviceName = "preview"
            ),
            onUpdateMoistureMin = {},
            onUpdateMoistureMax = {},
            onUpdateTemperatureMin = {},
            onUpdateTemperatureMax = {},
            onUpdateWifiCredentials = { _, _ -> },
            onNavigateBack = {}
        )
    }
}
