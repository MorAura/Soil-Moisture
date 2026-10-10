package com.moraura.soilapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import com.moraura.soilapp.ui.SoilSenseViewModel
import com.moraura.soilapp.ui.theme.SoilAppTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThresholdsScreen(
    viewModel: SoilSenseViewModel,
    onNavigateBack: () -> Unit
) {
    val thresholds by viewModel.thresholds.collectAsState()

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
                onMinChange = { viewModel.updateMoistureMin(it) },
                onMaxChange = { viewModel.updateMoistureMax(it) }
            )

            ThresholdCategory(
                title = "Temperature Thresholds",
                icon = Icons.Rounded.Thermostat,
                min = thresholds.temperatureMin,
                max = thresholds.temperatureMax,
                range = 0f..50f,
                unit = "°C",
                onMinChange = { viewModel.updateTemperatureMin(it) },
                onMaxChange = { viewModel.updateTemperatureMax(it) }
            )

            WifiConfigCategory(
                ssid = thresholds.wifiSsid,
                password = thresholds.wifiPassword,
                onSaveWifi = { ssid, password ->
                    viewModel.updateWifiCredentials(ssid, password)
                }
            )
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
            
            ThresholdSlider(
                label = "Minimum $unit",
                value = min,
                range = range,
                onValueChange = onMinChange
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            ThresholdSlider(
                label = "Maximum $unit",
                value = max,
                range = range,
                onValueChange = onMaxChange
            )
        }
    }
}

@Composable
fun ThresholdSlider(
    label: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Int) -> Unit
) {
    var sliderValue by remember(value) { mutableFloatStateOf(value.toFloat()) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${sliderValue.roundToInt()}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChange(sliderValue.roundToInt()) },
            valueRange = range,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ThresholdsScreenPreview() {
    SoilAppTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ThresholdCategory(
                "Moisture Thresholds",
                Icons.Rounded.WaterDrop,
                30, 70, 0f..100f, "%", {}, {}
            )
            WifiConfigCategory(
                ssid = "MyHomeWiFi",
                password = "Password123",
                onSaveWifi = { _, _ -> }
            )
        }
    }
}
