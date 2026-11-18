package io.github.amirisback.androidapp.core.game.render

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.lifecycle.compose.LifecycleResumeEffect
import io.github.amirisback.androidapp.core.game.loop.FixedTimestepClock
import kotlinx.coroutines.isActive

private const val NANOS_PER_SECOND = 1_000_000_000f

@Composable
fun GameCanvas(
    modifier: Modifier = Modifier,
    isRunning: Boolean = true,
    fixedStepSeconds: Float = FixedTimestepClock.DEFAULT_FIXED_STEP_SECONDS,
    onFixedUpdate: (Float) -> Unit,
    onDraw: DrawScope.(alpha: Float) -> Unit,
) {
    val currentFixedUpdate by rememberUpdatedState(onFixedUpdate)
    val currentDraw by rememberUpdatedState(onDraw)

    var isResumed by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        isResumed = true
        onPauseOrDispose {
            isResumed = false
        }
    }

    val clock = remember(fixedStepSeconds) {
        FixedTimestepClock(fixedStepSeconds = fixedStepSeconds)
    }
    val frameTick = remember { mutableLongStateOf(0L) }
    val renderAlpha = remember { mutableFloatStateOf(0f) }

    val shouldTick = isRunning && isResumed
    LaunchedEffect(shouldTick, clock) {
        if (!shouldTick) return@LaunchedEffect
        var lastFrameNanos = 0L
        while (isActive) {
            withFrameNanos { frameNanos ->
                if (lastFrameNanos == 0L) {
                    lastFrameNanos = frameNanos
                    frameTick.longValue = frameNanos
                    return@withFrameNanos
                }
                val dtSeconds = (frameNanos - lastFrameNanos) / NANOS_PER_SECOND
                lastFrameNanos = frameNanos
                renderAlpha.floatValue = clock.step(dtSeconds) { stepDt ->
                    currentFixedUpdate(stepDt)
                }
                frameTick.longValue = frameNanos
            }
        }
    }

    Canvas(modifier = modifier) {
        frameTick.longValue
        currentDraw(this, renderAlpha.floatValue)
    }
}
