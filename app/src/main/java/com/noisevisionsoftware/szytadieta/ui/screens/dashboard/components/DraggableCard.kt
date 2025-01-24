package com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.domain.model.dashboard.DashboardCardType
import kotlin.math.roundToInt

@Composable
fun DraggableCard(
    index: Int,
    isEditMode: Boolean,
    onMove: (Int, Int) -> Unit,
    content: @Composable () -> Unit
) {
    var isDragged by remember { mutableStateOf(false) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val cardHeight = remember { mutableIntStateOf(0) }
    val maxOffset = remember { mutableFloatStateOf(0f) }

    val animatedOffset by animateFloatAsState(
        targetValue = if (!isDragged) 0f else offsetY,
        label = "offset"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .offset { IntOffset(0, animatedOffset.roundToInt()) }
            .graphicsLayer {
                if (isDragged) {
                    scaleX = 1.02f
                    scaleY = 1.02f
                    shadowElevation = 8f
                }
            }
            .onSizeChanged { size ->
                cardHeight.intValue = size.height
                maxOffset.floatValue = size.height.toFloat()
            }
            .then(
                if (isEditMode) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                isDragged = true
                            },
                            onDragEnd = {
                                isDragged = false
                                if (cardHeight.intValue > 0) {
                                    val movement =
                                        (offsetY / (cardHeight.intValue * 0.7f)).roundToInt()

                                    if (movement != 0) {
                                        val toPosition = (index + movement)
                                            .coerceIn(0, DashboardCardType.entries.size - 1)

                                        if (index != toPosition) {
                                            onMove(index, toPosition)
                                        }
                                    }
                                }
                                offsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val newOffset = (offsetY + dragAmount.y).coerceIn(
                                    -maxOffset.floatValue * 1.5f,
                                    maxOffset.floatValue * 1.5f
                                )

                                offsetY = when {
                                    newOffset > maxOffset.floatValue -> {
                                        maxOffset.floatValue + (newOffset - maxOffset.floatValue) * 0.2f
                                    }

                                    newOffset < -maxOffset.floatValue -> {
                                        -maxOffset.floatValue + (newOffset + maxOffset.floatValue) * 0.2f
                                    }

                                    else -> newOffset
                                }
                            }
                        )
                    }
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragged) 8.dp else 1.dp
        )
    ) {
        Box {
            content()

            if (isEditMode) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Przeciągnij, aby zmienić kolejność",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .size(30.dp)
                            .alpha(0.8f)
                    )
                }
            }
        }
    }
}