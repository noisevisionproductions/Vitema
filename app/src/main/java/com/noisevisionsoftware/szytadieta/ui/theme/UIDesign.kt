package com.noisevisionsoftware.szytadieta.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun PatternBackground(
    modifier: Modifier = Modifier,
    patternColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val pattern = Path().apply {
            for (i in 0..size.width.toInt() step 20) {
                for (j in 0..size.height.toInt() step 20) {
                    moveTo(i.toFloat(), j.toFloat())
                    lineTo(i.toFloat() + 5, j.toFloat() + 5)
                }
            }
        }
        drawPath(
            path = pattern,
            color = patternColor,
            style = Stroke(width = 1f)
        )
    }
}

fun Modifier.topShadow() = this
    .graphicsLayer(clip = false)
    .drawWithContent {
        drawContent()
        val shadowHeight = 8.dp.toPx()
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.4f)
                ),
                startY = -shadowHeight,
                endY = 0f
            ),
            topLeft = Offset(0f, -shadowHeight),
            size = Size(size.width, shadowHeight)
        )
    }