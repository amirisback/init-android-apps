package io.github.amirisback.androidapp.feature.game

import io.github.amirisback.androidapp.core.game.ads.GameReward
import io.github.amirisback.androidapp.core.game.ads.RewardedAdState
import io.github.amirisback.androidapp.core.game.loop.GameLoopState

data class GameUiState(
    val score: Int = 0,
    val highScore: Int = 0,
    val coins: Int = 0,
    val level: Int = 1,
    val loopState: GameLoopState = GameLoopState.RUNNING,
    val sfxEnabled: Boolean = true,
    val bgmEnabled: Boolean = true,
    val rewardedAdState: RewardedAdState = RewardedAdState.Idle,
    val lastEarnedReward: GameReward? = null,
)
