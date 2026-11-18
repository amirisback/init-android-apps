package io.github.amirisback.androidapp.testutil

import android.app.Activity
import io.github.amirisback.androidapp.core.game.ads.GameReward
import io.github.amirisback.androidapp.core.game.ads.RewardedAdController
import io.github.amirisback.androidapp.core.game.ads.RewardedAdState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeRewardedAdController(
    initialState: RewardedAdState = RewardedAdState.Idle,
) : RewardedAdController {

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<RewardedAdState> = _state.asStateFlow()

    var preloadCount: Int = 0
        private set

    var nextReward: GameReward = GameReward(type = "coins", amount = 25)

    fun setState(newState: RewardedAdState) {
        _state.value = newState
    }

    override fun preload() {
        preloadCount++
        _state.value = RewardedAdState.Ready
    }

    override fun show(
        activity: Activity,
        onRewardEarned: (GameReward) -> Unit,
        onDismissed: () -> Unit,
    ) {
        if (_state.value is RewardedAdState.Ready) {
            _state.value = RewardedAdState.Showing
            onRewardEarned(nextReward)
            _state.value = RewardedAdState.Idle
            onDismissed()
        } else {
            onDismissed()
        }
    }
}
