package com.noisevisionsoftware.szytadieta.ui.common.animation

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun CelebrationEffect(
    show: Boolean,
    onAnimationEnd: () -> Unit = {},
    durationMillis: Long = 3000,
    enableVibration: Boolean = true,
    confettiColors: List<Int> = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def)
) {
    val context = LocalContext.current

    LaunchedEffect(show) {
        if (show) {
            if (enableVibration){
                context.vibrate()
            }
            delay(durationMillis)
            onAnimationEnd()
        }
    }

    if (show) {
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = listOf(
                Party(
                    speed = 5f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    colors = confettiColors,
                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                    position = Position.Relative(0.5, 0.3)
                )
            )
        )
    }
}

private fun Context.vibrate(duration: Long = 100) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    vibrator.vibrate(
        VibrationEffect.createOneShot(
            duration,
            VibrationEffect.DEFAULT_AMPLITUDE
        )
    )
}
