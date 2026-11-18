package io.github.amirisback.androidapp.core.game.loop

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class GameLoopState {
    IDLE,
    RUNNING,
    PAUSED,
}

class GameLoopController(
    initialState: GameLoopState = GameLoopState.IDLE,
) {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<GameLoopState> = _state.asStateFlow()

    fun start() {
        _state.value = GameLoopState.RUNNING
    }

    fun pause() {
        _state.update { current ->
            if (current == GameLoopState.RUNNING) GameLoopState.PAUSED else current
        }
    }

    fun resume() {
        _state.update { current ->
            if (current == GameLoopState.PAUSED) GameLoopState.RUNNING else current
        }
    }

    fun togglePause() {
        _state.update { current ->
            when (current) {
                GameLoopState.RUNNING -> GameLoopState.PAUSED
                GameLoopState.PAUSED, GameLoopState.IDLE -> GameLoopState.RUNNING
            }
        }
    }

    fun reset() {
        _state.value = GameLoopState.IDLE
    }
}
