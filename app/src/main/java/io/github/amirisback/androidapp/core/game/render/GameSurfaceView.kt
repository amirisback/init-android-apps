package io.github.amirisback.androidapp.core.game.render

import android.content.Context
import android.graphics.Canvas
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LifecycleResumeEffect
import io.github.amirisback.androidapp.core.game.loop.FixedTimestepClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val SURFACE_NANOS_PER_SECOND = 1_000_000_000f
private const val NANOS_PER_MILLI = 1_000_000L

class GameSurfaceView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private val surfaceLock = Any()
    private val renderDispatcher = Dispatchers.Default.limitedParallelism(1)

    @Volatile
    private var isRunning: Boolean = false

    @Volatile
    private var surfaceReady: Boolean = false

    @Volatile
    private var fixedUpdateCallback: (Float) -> Unit = {}

    @Volatile
    private var drawCallback: (Canvas, Float) -> Unit = { _, _ -> }

    private var configuredFixedStepSeconds: Float = FixedTimestepClock.DEFAULT_FIXED_STEP_SECONDS
    private var renderScope: CoroutineScope? = null
    private var renderJob: Job? = null

    init {
        holder.addCallback(this)
    }

    fun configure(
        isRunning: Boolean,
        fixedStepSeconds: Float,
        onFixedUpdate: (Float) -> Unit,
        onDraw: (Canvas, Float) -> Unit,
    ) {
        fixedUpdateCallback = onFixedUpdate
        drawCallback = onDraw

        val stepChanged = configuredFixedStepSeconds != fixedStepSeconds
        if (stepChanged) {
            configuredFixedStepSeconds = fixedStepSeconds
        }
        this.isRunning = isRunning

        if (surfaceReady && isRunning) {
            if (stepChanged || renderJob?.isActive != true) {
                launchRenderLoop(configuredFixedStepSeconds)
            }
        } else if (!isRunning) {
            stopLoop()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceReady = true
        if (isRunning) {
            launchRenderLoop(configuredFixedStepSeconds)
        }
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int,
    ) = Unit

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surfaceReady = false
        stopLoop()
        synchronized(surfaceLock) {
            // Drain any in-flight lockCanvas/unlockCanvasAndPost block before surface teardown.
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        surfaceReady = false
        stopLoop()
        renderScope?.cancel()
        renderScope = null
    }

    fun release() {
        surfaceReady = false
        stopLoop()
        synchronized(surfaceLock) {
            // Drain any in-flight canvas frame before releasing resources.
        }
        renderScope?.cancel()
        renderScope = null
        holder.removeCallback(this)
    }

    private fun launchRenderLoop(fixedStepSeconds: Float) {
        val scope = renderScope ?: CoroutineScope(SupervisorJob() + renderDispatcher).also {
            renderScope = it
        }
        val previousJob = renderJob
        renderJob = scope.launch {
            previousJob?.cancelAndJoin()

            val activeClock = FixedTimestepClock(fixedStepSeconds = fixedStepSeconds)
            val targetFrameMillis = (activeClock.fixedStepSeconds * 1000f).toLong().coerceAtLeast(1L)
            var lastNanos = System.nanoTime()

            while (isActive && surfaceReady && isRunning) {
                val frameStartNanos = System.nanoTime()
                val dtSeconds = (frameStartNanos - lastNanos) / SURFACE_NANOS_PER_SECOND
                lastNanos = frameStartNanos

                val alpha = activeClock.step(dtSeconds) { stepDt ->
                    fixedUpdateCallback(stepDt)
                }

                synchronized(surfaceLock) {
                    if (surfaceReady && holder.surface.isValid) {
                        val canvas = try {
                            holder.lockCanvas()
                        } catch (_: IllegalArgumentException) {
                            null
                        } catch (_: IllegalStateException) {
                            null
                        }
                        if (canvas != null) {
                            try {
                                drawCallback(canvas, alpha)
                            } finally {
                                try {
                                    holder.unlockCanvasAndPost(canvas)
                                } catch (_: IllegalArgumentException) {
                                    // Surface invalidated while frame was drawing.
                                } catch (_: IllegalStateException) {
                                    // Surface invalidated while frame was drawing.
                                }
                            }
                        }
                    }
                }

                val elapsedMillis = (System.nanoTime() - frameStartNanos) / NANOS_PER_MILLI
                val sleepMillis = targetFrameMillis - elapsedMillis
                if (sleepMillis > 0L) {
                    delay(sleepMillis)
                }
            }
        }
    }

    private fun stopLoop() {
        renderJob?.cancel()
        renderJob = null
    }
}

@Composable
fun GameSurfaceHost(
    modifier: Modifier = Modifier,
    isRunning: Boolean = true,
    fixedStepSeconds: Float = FixedTimestepClock.DEFAULT_FIXED_STEP_SECONDS,
    onFixedUpdate: (Float) -> Unit,
    onDraw: (Canvas, Float) -> Unit,
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

    val shouldTick = isRunning && isResumed
    AndroidView(
        modifier = modifier,
        factory = { context -> GameSurfaceView(context) },
        update = { view ->
            view.configure(
                isRunning = shouldTick,
                fixedStepSeconds = fixedStepSeconds,
                onFixedUpdate = currentFixedUpdate,
                onDraw = currentDraw,
            )
        },
        onRelease = { view -> view.release() },
    )
}
