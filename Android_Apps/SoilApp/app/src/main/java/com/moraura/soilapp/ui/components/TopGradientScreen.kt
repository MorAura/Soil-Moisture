package com.moraura.soilapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.moraura.soilapp.ui.theme.SoilAppTheme

/**
 * A container layout that renders animated soft color blobs at the top with an eased gradient
 * transition into the background color.
 *
 * @param modifier Modifier for the root container.
 * @param colors List of colors to display in the top gradient. Defaults to theme-appropriate soft palette.
 * @param gradientHeightFraction Fraction of screen height occupied by the top gradient (default 0.30f).
 * @param content Screen content placed inside the container.
 */
@Composable
fun TopGradientScreen(
    modifier: Modifier = Modifier,
    colors: List<Color> = if (isSystemInDarkTheme()) listOf(
        Color(0xFF1B4B5A),   // teal
        Color(0xFF1B2F5A),   // navy
        Color(0xFF2A1B4B)    // muted purple
    ) else listOf(
        Color(0xFFB2EBF2),   // teal
        Color(0xFFBBDEFB),   // navy
        Color(0xFFD1C4E9)    // purple
    ),
    gradientHeightFraction: Float = 0.30f,
    content: @Composable () -> Unit
) {
    val bg = MaterialTheme.colorScheme.background

    // one animated state per color; read later in the draw phase only
    val animated = colors.mapIndexed { i, c ->
        animateColorAsState(c, tween(600), label = "tint$i")
    }

    Box(modifier = modifier.fillMaxSize().background(bg)) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(gradientHeightFraction)
                .drawBehind {
                    // 1. soft color blobs spread across the top edge
                    animated.forEachIndexed { i, state ->
                        val x = if (animated.size == 1) size.width / 2
                        else size.width * i / (animated.size - 1)
                        drawRect(
                            Brush.radialGradient(
                                colors = listOf(state.value, Color.Transparent),
                                center = Offset(x, 0f),
                                radius = size.width * 0.9f
                            )
                        )
                    }
                    // 2. eased fade into the background color
                    drawRect(
                        Brush.verticalGradient(
                            colorStops = Array(13) { i ->
                                val t = i / 12f
                                val eased = t * t * (3f - 2f * t)
                                t to bg.copy(alpha = eased)
                            }
                        )
                    )
                }
        )

        content()
    }
}

@Preview(showBackground = true)
@Composable
fun TopGradientScreenPreview() {
    SoilAppTheme {
        TopGradientScreen {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Top Gradient Screen Content",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
