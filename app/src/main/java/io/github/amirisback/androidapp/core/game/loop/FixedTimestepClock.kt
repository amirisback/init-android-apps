package io.github.amirisback.androidapp.core.game.loop

class FixedTimestepClock(
    val fixedStepSeconds: Float = DEFAULT_FIXED_STEP_SECONDS,
    val maxFrameDeltaSeconds: Float = DEFAULT_MAX_FRAME_DELTA_SECONDS,
    val maxStepsPerFrame: Int = DEFAULT_MAX_STEPS_PER_FRAME,
) {

    init {
        require(fixedStepSeconds.isFinite() && fixedStepSeconds > 0f) {
            "fixedStepSeconds must be finite and > 0"
        }
        require(maxFrameDeltaSeconds.isFinite() && maxFrameDeltaSeconds > 0f) {
            "maxFrameDeltaSeconds must be finite and > 0"
        }
        require(maxStepsPerFrame > 0) {
            "maxStepsPerFrame must be > 0"
        }
    }

    private var accumulatorSeconds: Float = 0f

    fun step(
        frameDeltaSeconds: Float,
        onFixedUpdate: (Float) -> Unit,
    ): Float {
        val clampedDelta = if (frameDeltaSeconds.isFinite()) {
            frameDeltaSeconds.coerceIn(0f, maxFrameDeltaSeconds)
        } else {
            0f
        }
        accumulatorSeconds += clampedDelta

        var stepsExecuted = 0
        while (accumulatorSeconds >= fixedStepSeconds && stepsExecuted < maxStepsPerFrame) {
            onFixedUpdate(fixedStepSeconds)
            accumulatorSeconds -= fixedStepSeconds
            stepsExecuted++
        }

        if (stepsExecuted == maxStepsPerFrame && accumulatorSeconds >= fixedStepSeconds) {
            accumulatorSeconds = 0f
        }

        return (accumulatorSeconds / fixedStepSeconds).coerceIn(0f, 1f)
    }

    fun reset() {
        accumulatorSeconds = 0f
    }

    companion object {
        const val DEFAULT_FIXED_STEP_SECONDS: Float = 1f / 60f
        const val DEFAULT_MAX_FRAME_DELTA_SECONDS: Float = 0.25f
        const val DEFAULT_MAX_STEPS_PER_FRAME: Int = 5
    }
}
