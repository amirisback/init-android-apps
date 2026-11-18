package io.github.amirisback.androidapp.feature.game

import io.github.amirisback.androidapp.core.game.ads.GameReward
import io.github.amirisback.androidapp.core.game.ads.RewardedAdState
import io.github.amirisback.androidapp.core.game.loop.GameLoopState
import io.github.amirisback.androidapp.core.game.save.GameSaveSnapshot
import io.github.amirisback.androidapp.testutil.FakeGameAudioController
import io.github.amirisback.androidapp.testutil.FakeGameSaveStore
import io.github.amirisback.androidapp.testutil.FakeRewardedAdController
import io.github.amirisback.androidapp.testutil.MainDispatcherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initializes state from persisted snapshot and syncs audio flags`() {
        val saveStore = FakeGameSaveStore(
            GameSaveSnapshot(
                highScore = 15,
                currentLevel = 3,
                coins = 80,
                sfxEnabled = false,
                bgmEnabled = true,
            ),
        )
        val audio = FakeGameAudioController()
        val ads = FakeRewardedAdController()

        val viewModel = GameViewModel(saveStore, audio, ads)

        val state = viewModel.uiState.value
        assertEquals(0, state.score)
        assertEquals(15, state.highScore)
        assertEquals(3, state.level)
        assertEquals(80, state.coins)
        assertFalse(state.sfxEnabled)
        assertTrue(state.bgmEnabled)
        assertFalse(audio.sfxEnabled)
        assertTrue(audio.bgmEnabled)
    }

    @Test
    fun `onScoreIncrement updates score highScore plays sfx and ignores hits when paused`() {
        val saveStore = FakeGameSaveStore(GameSaveSnapshot(highScore = 1))
        val audio = FakeGameAudioController()
        val ads = FakeRewardedAdController()
        val viewModel = GameViewModel(saveStore, audio, ads)

        viewModel.onScoreIncrement()
        viewModel.onScoreIncrement()

        assertEquals(2, viewModel.uiState.value.score)
        assertEquals(2, viewModel.uiState.value.highScore)
        assertEquals(2, saveStore.currentSnapshot.highScore)
        assertEquals(
            listOf(GameViewModel.SFX_TARGET_HIT, GameViewModel.SFX_TARGET_HIT),
            audio.playedSfx,
        )

        viewModel.onTogglePause()
        assertEquals(GameLoopState.PAUSED, viewModel.uiState.value.loopState)

        viewModel.onScoreIncrement()
        assertEquals(2, viewModel.uiState.value.score)
    }

    @Test
    fun `onScoreIncrement ignores non-positive points`() {
        val saveStore = FakeGameSaveStore()
        val audio = FakeGameAudioController()
        val ads = FakeRewardedAdController()
        val viewModel = GameViewModel(saveStore, audio, ads)

        viewModel.onScoreIncrement(0)
        viewModel.onScoreIncrement(-5)

        assertEquals(0, viewModel.uiState.value.score)
        assertEquals(0, viewModel.uiState.value.highScore)
        assertTrue(audio.playedSfx.isEmpty())
    }

    @Test
    fun `onToggleSfx and onToggleBgm update controller and persist settings`() {
        val saveStore = FakeGameSaveStore()
        val audio = FakeGameAudioController()
        val ads = FakeRewardedAdController()
        val viewModel = GameViewModel(saveStore, audio, ads)

        viewModel.onToggleSfx()
        assertFalse(viewModel.uiState.value.sfxEnabled)
        assertFalse(audio.sfxEnabled)
        assertFalse(saveStore.currentSnapshot.sfxEnabled)

        viewModel.onToggleBgm()
        assertFalse(viewModel.uiState.value.bgmEnabled)
        assertFalse(audio.bgmEnabled)
        assertFalse(saveStore.currentSnapshot.bgmEnabled)
    }

    @Test
    fun `onRewardEarned adds coins emits structured reward event and ignores non-positive amounts`() {
        val saveStore = FakeGameSaveStore(GameSaveSnapshot(coins = 10))
        val audio = FakeGameAudioController()
        val ads = FakeRewardedAdController()
        val viewModel = GameViewModel(saveStore, audio, ads)

        viewModel.onPreloadRewardedAd()
        assertEquals(1, ads.preloadCount)
        assertEquals(RewardedAdState.Ready, viewModel.uiState.value.rewardedAdState)

        viewModel.onRewardEarned(GameReward(type = "coins", amount = -10))
        viewModel.onRewardEarned(GameReward(type = "coins", amount = 0))
        assertEquals(10, viewModel.uiState.value.coins)
        assertNull(viewModel.uiState.value.lastEarnedReward)

        val earned = GameReward(type = "coins", amount = 25)
        viewModel.onRewardEarned(earned)
        assertEquals(35, viewModel.uiState.value.coins)
        assertEquals(35, saveStore.currentSnapshot.coins)
        assertEquals(earned, viewModel.uiState.value.lastEarnedReward)

        viewModel.consumeEarnedReward()
        assertNull(viewModel.uiState.value.lastEarnedReward)
    }
}
