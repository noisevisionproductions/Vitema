package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import kotlinx.coroutines.delay

/*
* Komponent wyświetlający pojedyncze zdjęcie przepisu z animacją ładowania
* */
@Composable
fun RecipeImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    title: String = "",
    contentScale: ContentScale = ContentScale.Crop,
    onImageClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onImageClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = title.ifEmpty { "Zdjęcie przepisu" },
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        ShimmerLoadingAnimation()
                    }

                    is AsyncImagePainter.State.Error -> {
                        ImageErrorPlaceholder()
                    }

                    else -> {
                        SubcomposeAsyncImageContent()
                    }
                }
            }

            if (title.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/*
* Animacja ładowania ze wzorem "shimmer
* */
@Composable
private fun ShimmerLoadingAnimation(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(10f, 10f),
        end = Offset(translateAnim.value, translateAnim.value)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush)
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/*
* Placeholder wyświetlany w przypadku błędu ładowania zdjęcia
* */
@Composable
private fun ImageErrorPlaceholder(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Nie udało się załadować zdjęcia",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/*
* Karuzela zdjęć z możliwościa przewijania i powiększania
* */
@Composable
fun RecipeImagesCarousel(
    photos: List<String>,
    modifier: Modifier = Modifier,
    initialDelayMillis: Long = 200,
    containerHeight: Float = 280f
) {
    if (photos.isEmpty()) return

    var isLoaded by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { photos.size })
    var showFullScreenImage by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        delay(initialDelayMillis)
        isLoaded = true
    }

    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage
    }

    AnimatedVisibility(
        visible = isLoaded,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(containerHeight.dp)
                    .padding(vertical = 8.dp)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photos[page])
                                .crossfade(true)
                                .build(),
                            contentDescription = "Zdjęcie przepisu ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .align(Alignment.Center)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedImageUrl = photos[page]
                                    showFullScreenImage
                                }
                        ) {
                            when (painter.state) {
                                is AsyncImagePainter.State.Loading -> {
                                    ShimmerLoadingAnimation()
                                }

                                is AsyncImagePainter.State.Error -> {
                                    ImageErrorPlaceholder()
                                }

                                else -> {
                                    SubcomposeAsyncImageContent()
                                }
                            }
                        }
                    }
                }
            }

            if (photos.size > 1) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(photos.size) { iteration ->
                        val color = if (currentPage == iteration) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(if (currentPage == iteration) 10.dp else 8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showFullScreenImage) {
        FullScreenImageDialog(
            imageUrl = selectedImageUrl,
            onDismiss = { showFullScreenImage = false }
        )
    }
}

/*
* Dialog pełnoekranowy do powiększania zdjęć
* */
@Composable
private fun FullScreenImageDialog(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f))
                .clickable(onClick = onDismiss)
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .zIndex(10f)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Zamknij",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            var scale by remember { mutableFloatStateOf(1f) }
            var offset by remember { mutableStateOf(Offset.Zero) }
            val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)
                // Ograniczenie przesunięcia w zależności od skali
                val maxX = (scale - 1) * 1000
                val maxY = (scale - 1) * 1000
                val newOffsetX = offset.x + offsetChange.x
                val newOffsetY = offset.y + offsetChange.y
                offset = Offset(
                    x = newOffsetX.coerceIn(-maxX, maxX),
                    y = newOffsetY.coerceIn(-maxY, maxY)
                )
            }

            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Powiększone zdjęcie",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(transformableState)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    is AsyncImagePainter.State.Error -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nie udało się załadować zdjęcia",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    else -> {
                        SubcomposeAsyncImageContent()
                    }
                }
            }
        }
    }
}

/**
 * Kompaktowa karuzela zdjęć do użycia w kartach posiłków
 */
@Composable
fun CompactImagesCarousel(
    photos: List<String>,
    modifier: Modifier = Modifier,
    maxHeight: Float = 160f
) {
    if (photos.isEmpty()) return

    var currentPage by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { photos.size })
    var showFullScreenImage by remember { mutableStateOf(false) }
    var selectedImageUrl by remember { mutableStateOf("") }

    LaunchedEffect(pagerState.currentPage) {
        currentPage = pagerState.currentPage
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photos[page])
                        .crossfade(true)
                        .build(),
                    contentDescription = "Zdjęcie przepisu ${page + 1}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            selectedImageUrl = photos[page]
                            showFullScreenImage = true
                        }
                ) {
                    when (painter.state) {
                        is AsyncImagePainter.State.Loading -> {
                            ShimmerLoadingAnimation()
                        }

                        is AsyncImagePainter.State.Error -> {
                            ImageErrorPlaceholder()
                        }

                        else -> {
                            SubcomposeAsyncImageContent()
                        }
                    }
                }
            }
        }

        // Minimalistyczne wskaźniki
        if (photos.size > 1) {
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(photos.size) { iteration ->
                    val color = if (currentPage == iteration) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(if (currentPage == iteration) 8.dp else 6.dp)
                    )
                }
            }
        }
    }

    // Tryb pełnoekranowy
    if (showFullScreenImage) {
        FullScreenImageDialog(
            imageUrl = selectedImageUrl,
            onDismiss = { showFullScreenImage = false }
        )
    }
}