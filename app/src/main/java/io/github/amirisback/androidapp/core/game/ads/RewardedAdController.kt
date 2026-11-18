package io.github.amirisback.androidapp.core.game.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface RewardedAdController {
    val state: StateFlow<RewardedAdState>
    fun preload()
    fun show(
        activity: Activity,
        onRewardEarned: (GameReward) -> Unit,
        onDismissed: () -> Unit = {},
    )
}
