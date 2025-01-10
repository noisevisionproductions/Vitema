package com.noisevisionsoftware.szytadieta.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PatternBackground(
    modifier: Modifier = Modifier,
    patternColor: Color = LocalPatternColor.current,
    backgroundColor: Color = LocalBackgroundColor.current
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(color = backgroundColor)

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

fun Modifier.topShadow(
    color: Color = Color.Black.copy(alpha = 0.1f),
    height: Dp = 8.dp,
    offsetY: Dp = 2.dp
) = this.drawBehind {
    val shadowHeight = height.toPx()
    val offsetYPx = offsetY.toPx()

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                color,
                Color.Transparent
            ),
            startY = 0f,
            endY = shadowHeight
        ),
        topLeft = Offset(0f, offsetYPx),
        size = Size(
            width = size.width,
            height = shadowHeight
        )
    )
}