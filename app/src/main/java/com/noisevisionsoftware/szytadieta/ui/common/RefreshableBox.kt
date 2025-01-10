package com.noisevisionsoftware.szytadieta.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun RefreshableBox(
    onRefresh: () -> Unit,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val refreshState = remember { RefreshState() }
    val offset by animateFloatAsState(
        targetValue = if (isRefreshing) refreshState.refreshThreshold else refreshState.offset,
        label = "Refresh animation"
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < 0 || refreshState.offset == 0f) return Offset.Zero
                if (isRefreshing) return Offset.Zero

                val newOffset =
                    (refreshState.offset + available.y).coerceIn(0f, refreshState.maxOffset)
                refreshState.offset = newOffset
                return available
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (refreshState.offset > refreshState.refreshThreshold) {
                    onRefresh()
                }
                refreshState.offset = 0f
                return super.onPostFling(consumed, available)
            }
        }
    }

    Box(
        modifier = modifier
            .nestedScroll(nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier
                .offset { IntOffset(0, offset.roundToInt()) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (offset > 0) {
                    val progress = (offset / refreshState.refreshThreshold).coerceIn(0f, 1f)
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                    )
                }
            }
            content()
        }
    }
}

private class RefreshState {
    var offset by mutableFloatStateOf(0f)
    val maxOffset = 150f
    val refreshThreshold = 100f
}