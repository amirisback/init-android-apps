package io.github.amirisback.androidapp.core.game.save

data class GameSaveSnapshot(
    val highScore: Int = 0,
    val currentLevel: Int = 1,
    val coins: Int = 0,
    val sfxEnabled: Boolean = true,
    val bgmEnabled: Boolean = true,
    val customStateJson: String = "",
)
