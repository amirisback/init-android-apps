package io.github.amirisback.androidapp.core.game.loop

import org.junit.Assert.assertEquals
import org.junit.Test

class GameLoopControllerTest {

    @Test
    fun `transitions across IDLE RUNNING PAUSED and reset`() {
        val controller = GameLoopController()

        assertEquals(GameLoopState.IDLE, controller.state.value)

        // pause and resume are no-ops from IDLE
        controller.pause()
        assertEquals(GameLoopState.IDLE, controller.state.value)
        controller.resume()
        assertEquals(GameLoopState.IDLE, controller.state.value)

        controller.start()
        assertEquals(GameLoopState.RUNNING, controller.state.value)

        controller.pause()
        assertEquals(GameLoopState.PAUSED, controller.state.value)

        controller.resume()
        assertEquals(GameLoopState.RUNNING, controller.state.value)

        controller.togglePause()
        assertEquals(GameLoopState.PAUSED, controller.state.value)

        controller.togglePause()
        assertEquals(GameLoopState.RUNNING, controller.state.value)

        controller.reset()
        assertEquals(GameLoopState.IDLE, controller.state.value)

        controller.togglePause()
        assertEquals(GameLoopState.RUNNING, controller.state.value)
    }
}
