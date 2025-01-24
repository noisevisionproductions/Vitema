package com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun SpotlightOverlay(
    visible: Boolean,
    targetBounds: Rect,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    if (visible) {
        val spotlightPadding = 12.dp
        val spotlightSize = with(LocalDensity.current) {
            (targetBounds.width + (spotlightPadding * 2).toPx()) to
                    (targetBounds.height + (spotlightPadding * 2).toPx())
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
            )

            Box(
                modifier = Modifier
                    .size(
                        with(LocalDensity.current) { spotlightSize.first.toDp() },
                        with(LocalDensity.current) { spotlightSize.second.toDp() }
                    )
                    .offset(
                        x = with(LocalDensity.current) {
                            (targetBounds.center.x - spotlightSize.first / 2).toDp()
                        },
                        y = with(LocalDensity.current) {
                            (targetBounds.center.y - spotlightSize.second / 2).toDp()
                        }
                    )
                    .background(
                        color = Color.Transparent,
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(
                        with(LocalDensity.current) { targetBounds.width.toDp() },
                        with(LocalDensity.current) { targetBounds.height.toDp() }
                    )
                    .offset(
                        x = with(LocalDensity.current) { targetBounds.left.toDp() },
                        y = with(LocalDensity.current) { targetBounds.top.toDp() }
                    )
                    .background(Color.Transparent)
            )

            content()
        }
    }
}

@Composable
fun AnimatedSpotlightOverlay(
    visible: Boolean,
    targetBounds: Rect,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300))
    ) {
        SpotlightOverlay(
            visible = true,
            targetBounds = targetBounds,
            onDismiss = onDismiss,
            content = content
        )
    }
}

@Composable
fun TutorialTooltip(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Możesz dostosować układ dashboardu!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Kliknij ikonę edycji, a następnie przeciągaj karty, aby zmienić ich kolejność.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )

                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Rozumiem")
                }
            }
        }
    }
}