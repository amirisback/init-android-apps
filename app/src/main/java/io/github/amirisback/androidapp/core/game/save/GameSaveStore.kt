package io.github.amirisback.androidapp.core.game.save

import kotlinx.coroutines.flow.Flow

interface GameSaveStore {
    val snapshot: Flow<GameSaveSnapshot>
    suspend fun updateHighScore(score: Int)
    suspend fun setCurrentLevel(level: Int)
    suspend fun addCoins(delta: Int)
    suspend fun setAudioSettings(sfxEnabled: Boolean, bgmEnabled: Boolean)
    suspend fun saveCustomStateJson(json: String)
    suspend fun clear()
}
