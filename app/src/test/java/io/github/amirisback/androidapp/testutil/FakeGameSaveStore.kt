package io.github.amirisback.androidapp.testutil

import io.github.amirisback.androidapp.core.game.save.GameSaveSnapshot
import io.github.amirisback.androidapp.core.game.save.GameSaveStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeGameSaveStore(
    initialSnapshot: GameSaveSnapshot = GameSaveSnapshot(),
) : GameSaveStore {

    private val _snapshot = MutableStateFlow(initialSnapshot)
    override val snapshot: Flow<GameSaveSnapshot> = _snapshot.asStateFlow()

    val currentSnapshot: GameSaveSnapshot
        get() = _snapshot.value

    override suspend fun updateHighScore(score: Int) {
        _snapshot.update { current ->
            if (score > current.highScore) current.copy(highScore = score) else current
        }
    }

    override suspend fun setCurrentLevel(level: Int) {
        _snapshot.update { it.copy(currentLevel = level.coerceAtLeast(1)) }
    }

    override suspend fun addCoins(delta: Int) {
        _snapshot.update { current ->
            current.copy(
                coins = (current.coins.toLong() + delta.toLong())
                    .coerceIn(0L, Int.MAX_VALUE.toLong())
                    .toInt(),
            )
        }
    }

    override suspend fun setAudioSettings(sfxEnabled: Boolean, bgmEnabled: Boolean) {
        _snapshot.update { it.copy(sfxEnabled = sfxEnabled, bgmEnabled = bgmEnabled) }
    }

    override suspend fun saveCustomStateJson(json: String) {
        _snapshot.update { it.copy(customStateJson = json) }
    }

    override suspend fun clear() {
        _snapshot.value = GameSaveSnapshot()
    }
}
