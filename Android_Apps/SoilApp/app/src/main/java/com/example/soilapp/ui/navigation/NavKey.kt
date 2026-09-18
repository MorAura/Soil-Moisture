package com.example.soilapp.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface SoilSenseRoute : NavKey {
    @Serializable
    data object DeviceScan : SoilSenseRoute
    
    @Serializable
    data object Dashboard : SoilSenseRoute

    @Serializable
    data object History : SoilSenseRoute

    @Serializable
    data object Thresholds : SoilSenseRoute
}
