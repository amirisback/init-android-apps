package io.github.amirisback.androidapp.core.game.ads

sealed interface RewardedAdState {
    data object Idle : RewardedAdState
    data object Loading : RewardedAdState
    data object Ready : RewardedAdState
    data object Showing : RewardedAdState
    data class Failed(val message: String) : RewardedAdState
}

data class GameReward(
    val type: String,
    val amount: Int,
)
