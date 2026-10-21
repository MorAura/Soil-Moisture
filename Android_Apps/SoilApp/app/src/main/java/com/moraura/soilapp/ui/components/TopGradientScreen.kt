package com.moraura.soilapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.moraura.soilapp.ui.theme.DarkForestBackground

@Composable
fun TopGradientScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkForestBackground)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Top-Left soft lime bokeh orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFB8E986).copy(alpha = 0.35f),
                        Color(0xFF82B84D).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.15f, height * 0.22f),
                    radius = width * 0.45f
                ),
                center = Offset(width * 0.15f, height * 0.22f),
                radius = width * 0.45f
            )

            // 2. Top-Right soft warm yellow-green bokeh orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFD4F28C).copy(alpha = 0.28f),
                        Color(0xFF6B9E38).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.85f, height * 0.12f),
                    radius = width * 0.40f
                ),
                center = Offset(width * 0.85f, height * 0.12f),
                radius = width * 0.40f
            )

            // 3. Middle-Right soft foliage bokeh orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF78B542).copy(alpha = 0.25f),
                        Color(0xFF395B1D).copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.75f, height * 0.42f),
                    radius = width * 0.50f
                ),
                center = Offset(width * 0.75f, height * 0.42f),
                radius = width * 0.50f
            )

            // 4. Bottom-Left glowing lime bokeh orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFA2DF52).copy(alpha = 0.32f),
                        Color(0xFF538228).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.10f, height * 0.78f),
                    radius = width * 0.48f
                ),
                center = Offset(width * 0.10f, height * 0.78f),
                radius = width * 0.48f
            )

            // 5. Center-Bottom deep moss bokeh orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF639732).copy(alpha = 0.20f),
                        Color(0xFF263D16).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.60f, height * 0.88f),
                    radius = width * 0.45f
                ),
                center = Offset(width * 0.60f, height * 0.88f),
                radius = width * 0.45f
            )
        }

        content()
    }
}
