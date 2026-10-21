package com.moraura.soilapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moraura.soilapp.data.model.SoilSenseMetrics
import com.moraura.soilapp.ui.SoilSenseViewModel
import com.moraura.soilapp.ui.theme.SoilAppTheme
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: SoilSenseViewModel,
    onNavigateBack: () -> Unit
) {
    val metrics by viewModel.metrics.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historical Data") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        HistoryContent(
            metrics = metrics,
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        )
    }
}

@Composable
fun HistoryContent(
    metrics: SoilSenseMetrics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ChartSection(
            title = "Soil Moisture",
            data = metrics.moisture,
            unit = "%",
            color = MaterialTheme.colorScheme.primary
        )

        ChartSection(
            title = "Temperature",
            data = metrics.temperature,
            unit = "°C",
            color = MaterialTheme.colorScheme.secondary
        )

        ChartSection(
            title = "Light Level",
            data = metrics.light,
            unit = "lux",
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun ChartSection(
    title: String,
    data: List<Int>,
    unit: String,
    color: Color
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            withContext(Dispatchers.Default) {
                modelProducer.runTransaction {
                    lineSeries {
                        series(data.asReversed().map { it.toFloat() })
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = color.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = unit,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = color
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Waiting for sensor data...", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberLineCartesianLayer(),
                        startAxis = rememberStartAxis(),
                        bottomAxis = rememberBottomAxis(),
                    ),
                    modelProducer = modelProducer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    SoilAppTheme {
        val mockData = List(168) { (20..80).random() }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Historical Data") },
                    navigationIcon = {
                        IconButton(onClick = { } ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            HistoryContent(
                metrics = SoilSenseMetrics(
                    moisture = mockData,
                    temperature = mockData,
                    light = mockData
                ),
                modifier = Modifier.padding(16.dp)
            )
        }

    }
}
