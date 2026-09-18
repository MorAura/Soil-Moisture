package com.example.soilapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.soilapp.data.model.ConnectionState
import com.example.soilapp.ui.SoilSenseViewModel
import com.example.soilapp.ui.navigation.SoilSenseRoute
import com.example.soilapp.ui.screens.DashboardScreen
import com.example.soilapp.ui.screens.DeviceScanScreen
import com.example.soilapp.ui.screens.HistoryScreen
import com.example.soilapp.ui.screens.ThresholdsScreen
import com.example.soilapp.ui.theme.SoilAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoilAppTheme {
                SoilSenseApp()
            }
        }
    }
}

@Composable
fun SoilSenseApp(viewModel: SoilSenseViewModel = viewModel()) {
    val context = LocalContext.current
    val permissionsToRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    // Function to check if all permissions are granted
    fun checkHasPermissions(): Boolean {
        return permissionsToRequest.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    var allPermissionsGranted by remember { mutableStateOf(checkHasPermissions()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        allPermissionsGranted = result.values.all { it }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                allPermissionsGranted = checkHasPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val connectionState by viewModel.connectionState.collectAsState()

    if (allPermissionsGranted) {
        val backStack = rememberNavBackStack(SoilSenseRoute.DeviceScan)

        // Automatically navigate to Dashboard when connected
        LaunchedEffect(connectionState) {
            if (connectionState is ConnectionState.Connected && backStack.lastOrNull() is SoilSenseRoute.DeviceScan) {
                backStack.add(SoilSenseRoute.Dashboard)
            } else if (connectionState is ConnectionState.Disconnected && backStack.lastOrNull() is SoilSenseRoute.Dashboard) {
                backStack.removeAt(backStack.size - 1)
            }
        }

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeAt(backStack.size - 1) },
            entryProvider = { key ->
                when (key) {
                    is SoilSenseRoute.DeviceScan -> NavEntry(key) {
                        DeviceScanScreen(
                            viewModel = viewModel,
                            onDeviceSelected = { device ->
                                viewModel.connect(device)
                            }
                        )
                    }
                    is SoilSenseRoute.Dashboard -> NavEntry(key) {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToHistory = { backStack.add(SoilSenseRoute.History) },
                            onNavigateToThresholds = { backStack.add(SoilSenseRoute.Thresholds) }
                        )
                    }
                    is SoilSenseRoute.History -> NavEntry(key) {
                        HistoryScreen(
                            viewModel = viewModel,
                            onNavigateBack = { backStack.removeAt(backStack.size - 1) }
                        )
                    }
                    is SoilSenseRoute.Thresholds -> NavEntry(key) {
                        ThresholdsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { backStack.removeAt(backStack.size - 1) }
                        )
                    }
                    else -> error("Unknown route: $key")
                }
            }
        )
    } else {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Permissions required for BLE scanning and connection.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { permissionLauncher.launch(permissionsToRequest.toTypedArray()) }) {
                    Icon(Icons.Rounded.Security, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Grant Permissions")
                }
            }
        }
    }
}
