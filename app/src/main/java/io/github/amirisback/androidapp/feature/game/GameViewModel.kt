package io.github.amirisback.androidapp.feature.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.amirisback.androidapp.core.game.ads.GameReward
import io.github.amirisback.androidapp.core.game.ads.RewardedAdController
import io.github.amirisback.androidapp.core.game.audio.GameAudioController
import io.github.amirisback.androidapp.core.game.loop.GameLoopController
import io.github.amirisback.androidapp.core.game.loop.GameLoopState
import io.github.amirisback.androidapp.core.game.save.GameSaveStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val saveStore: GameSaveStore,
    private val audioController: GameAudioController,
    private val rewardedAdController: RewardedAdController,
) : ViewModel() {

    private val loopController = GameLoopController(initialState = GameLoopState.RUNNING)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        saveStore.snapshot
            .onEach { snapshot ->
                audioController.setSfxEnabled(snapshot.sfxEnabled)
                audioController.setBgmEnabled(snapshot.bgmEnabled)
                _uiState.update { current ->
                    current.copy(
                        highScore = maxOf(current.highScore, snapshot.highScore),
                        coins = snapshot.coins,
                        level = snapshot.currentLevel,
                        sfxEnabled = snapshot.sfxEnabled,
                        bgmEnabled = snapshot.bgmEnabled,
                    )
                }
            }
            .launchIn(viewModelScope)

        loopController.state
            .onEach { loopState ->
                _uiState.update { it.copy(loopState = loopState) }
            }
            .launchIn(viewModelScope)

        rewardedAdController.state
            .onEach { adState ->
                _uiState.update { it.copy(rewardedAdState = adState) }
            }
            .launchIn(viewModelScope)
    }

    fun onScoreIncrement(points: Int = 1) {
        if (points <= 0 || _uiState.value.loopState != GameLoopState.RUNNING) return
        val nextScore = (_uiState.value.score.toLong() + points.toLong())
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        _uiState.update { current ->
            current.copy(
                score = nextScore,
                highScore = maxOf(current.highScore, nextScore),
            )
        }
        audioController.playSfx(SFX_TARGET_HIT)
        viewModelScope.launch {
            saveStore.updateHighScore(nextScore)
        }
    }

    fun onTogglePause() {
        loopController.togglePause()
    }

    fun onToggleSfx() {
        val current = _uiState.value
        val nextSfx = !current.sfxEnabled
        _uiState.update { it.copy(sfxEnabled = nextSfx) }
        audioController.setSfxEnabled(nextSfx)
        viewModelScope.launch {
            saveStore.setAudioSettings(sfxEnabled = nextSfx, bgmEnabled = current.bgmEnabled)
        }
    }

    fun onToggleBgm() {
        val current = _uiState.value
        val nextBgm = !current.bgmEnabled
        _uiState.update { it.copy(bgmEnabled = nextBgm) }
        audioController.setBgmEnabled(nextBgm)
        viewModelScope.launch {
            saveStore.setAudioSettings(sfxEnabled = current.sfxEnabled, bgmEnabled = nextBgm)
        }
    }

    fun onRewardEarned(reward: GameReward) {
        if (reward.amount <= 0) return
        _uiState.update {
            it.copy(lastEarnedReward = reward)
        }
        viewModelScope.launch {
            saveStore.addCoins(reward.amount)
        }
    }

    fun onPreloadRewardedAd() {
        rewardedAdController.preload()
    }

    fun consumeEarnedReward() {
        _uiState.update { it.copy(lastEarnedReward = null) }
    }

    companion object {
        const val SFX_TARGET_HIT = "target_hit"
    }
}
