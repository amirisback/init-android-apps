package io.github.amirisback.androidapp.core.game.loop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FixedTimestepClockTest {

    @Test
    fun `step executes exact number of fixed updates and returns interpolation alpha`() {
        val clock = FixedTimestepClock(fixedStepSeconds = 0.02f)
        val steps = mutableListOf<Float>()

        val alpha = clock.step(0.05f) { dt -> steps.add(dt) }

        assertEquals(2, steps.size)
        assertEquals(0.02f, steps[0], 1e-6f)
        assertEquals(0.02f, steps[1], 1e-6f)
        assertEquals(0.5f, alpha, 1e-4f)
    }

    @Test
    fun `step clamps large frame delta to maxFrameDeltaSeconds`() {
        val clock = FixedTimestepClock(
            fixedStepSeconds = 0.05f,
            maxFrameDeltaSeconds = 0.1f,
            maxStepsPerFrame = 10,
        )
        var stepCount = 0

        val alpha = clock.step(5.0f) { stepCount++ }

        assertEquals(2, stepCount)
        assertEquals(0f, alpha, 1e-4f)
    }

    @Test
    fun `step caps iterations at maxStepsPerFrame and drops overflow to prevent spiral of death`() {
        val clock = FixedTimestepClock(
            fixedStepSeconds = 0.02f,
            maxFrameDeltaSeconds = 1.0f,
            maxStepsPerFrame = 3,
        )
        var stepCount = 0

        val alpha = clock.step(0.5f) { stepCount++ }

        assertEquals(3, stepCount)
        assertEquals(0f, alpha, 1e-6f)

        var followUpSteps = 0
        clock.step(0.01f) { followUpSteps++ }
        assertEquals(0, followUpSteps)
    }

    @Test
    fun `step ignores negative and non-finite deltas`() {
        val clock = FixedTimestepClock(fixedStepSeconds = 0.02f)
        var stepCount = 0

        assertEquals(0f, clock.step(-0.1f) { stepCount++ }, 1e-6f)
        assertEquals(0f, clock.step(Float.NaN) { stepCount++ }, 1e-6f)
        assertEquals(0f, clock.step(Float.POSITIVE_INFINITY) { stepCount++ }, 1e-6f)
        assertEquals(0, stepCount)
    }

    @Test
    fun `reset clears accumulated remainder`() {
        val clock = FixedTimestepClock(fixedStepSeconds = 0.02f)
        var stepCount = 0

        val beforeResetAlpha = clock.step(0.015f) { stepCount++ }
        assertEquals(0, stepCount)
        assertEquals(0.75f, beforeResetAlpha, 1e-4f)

        clock.reset()
        val afterResetAlpha = clock.step(0.01f) { stepCount++ }
        assertEquals(0, stepCount)
        assertEquals(0.5f, afterResetAlpha, 1e-4f)
    }

    @Test
    fun `constructor rejects non-positive or non-finite arguments`() {
        assertThrows(IllegalArgumentException::class.java) {
            FixedTimestepClock(fixedStepSeconds = 0f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            FixedTimestepClock(fixedStepSeconds = -0.01f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            FixedTimestepClock(fixedStepSeconds = Float.NaN)
        }
        assertThrows(IllegalArgumentException::class.java) {
            FixedTimestepClock(maxFrameDeltaSeconds = 0f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            FixedTimestepClock(maxStepsPerFrame = 0)
        }
    }
}
