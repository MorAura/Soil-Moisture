package com.moraura.soilapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
                title = { Text("Threshold Configuration") },
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
        }
    }
}
