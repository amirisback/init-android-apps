package io.github.amirisback.androidapp.feature.game

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.amirisback.androidapp.R
import io.github.amirisback.androidapp.core.game.ads.RewardedAdState
import io.github.amirisback.androidapp.core.game.audio.GameAudioController
import io.github.amirisback.androidapp.core.game.audio.GameAudioLifecycleEffect
import io.github.amirisback.androidapp.core.game.loop.GameLoopState
import io.github.amirisback.androidapp.core.game.render.GameCanvas
import io.github.amirisback.androidapp.core.game.window.ImmersiveModeEffect
import io.github.amirisback.androidapp.core.ui.theme.InitTheme
import kotlin.math.hypot

@Composable
fun GameRoute(
    viewModel: GameViewModel,
    audioController: GameAudioController,
    onShowRewardedAd: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val resources = LocalResources.current

    ImmersiveModeEffect(enabled = true, keepScreenOn = true)
    GameAudioLifecycleEffect(audioController = audioController)

    LaunchedEffect(Unit) {
        viewModel.onPreloadRewardedAd()
    }

    LaunchedEffect(uiState.lastEarnedReward) {
        uiState.lastEarnedReward?.let { reward ->
            onShowMessage(
                resources.getString(R.string.game_reward_earned, reward.amount, reward.type),
            )
            viewModel.consumeEarnedReward()
        }
    }

    GameScreen(
        uiState = uiState,
        onTargetHit = viewModel::onScoreIncrement,
        onTogglePause = viewModel::onTogglePause,
        onToggleSfx = viewModel::onToggleSfx,
        onToggleBgm = viewModel::onToggleBgm,
        onWatchRewardedAd = onShowRewardedAd,
        onPreloadRewardedAd = viewModel::onPreloadRewardedAd,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameScreen(
    uiState: GameUiState,
    onTargetHit: () -> Unit,
    onTogglePause: () -> Unit,
    onToggleSfx: () -> Unit,
    onToggleBgm: () -> Unit,
    onWatchRewardedAd: () -> Unit,
    onPreloadRewardedAd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val simulation = remember { BouncingTargetSimulation() }
    val currentOnTargetHit by rememberUpdatedState(onTargetHit)
    val targetColor = MaterialTheme.colorScheme.primary
    val targetCoreColor = MaterialTheme.colorScheme.onPrimary
    val fieldColor = MaterialTheme.colorScheme.surfaceContainerLow

    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GameHudBar(uiState = uiState)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(color = fieldColor, shape = RoundedCornerShape(20.dp))
                    .onSizeChanged { size -> simulation.updateBounds(size) }
                    .pointerInput(uiState.loopState) {
                        detectTapGestures { tapOffset ->
                            if (uiState.loopState == GameLoopState.RUNNING &&
                                simulation.tryHit(tapOffset)
                            ) {
                                currentOnTargetHit()
                            }
                        }
                    },
            ) {
                GameCanvas(
                    modifier = Modifier.fillMaxSize(),
                    isRunning = uiState.loopState == GameLoopState.RUNNING,
                    onFixedUpdate = { dt -> simulation.step(dt) },
                    onDraw = { alpha ->
                        val center = simulation.interpolatedCenter(alpha)
                        drawCircle(
                            color = targetColor,
                            radius = simulation.radiusPx,
                            center = center,
                        )
                        drawCircle(
                            color = targetCoreColor,
                            radius = simulation.radiusPx * 0.35f,
                            center = center,
                        )
                    },
                )

                Text(
                    text = if (uiState.loopState == GameLoopState.PAUSED) {
                        stringResource(id = R.string.game_state_paused)
                    } else {
                        stringResource(id = R.string.game_hint_tap_target)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                )
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onTogglePause) {
                    Text(
                        text = stringResource(
                            id = if (uiState.loopState == GameLoopState.RUNNING) {
                                R.string.game_action_pause
                            } else {
                                R.string.game_action_resume
                            },
                        ),
                    )
                }

                OutlinedButton(onClick = onToggleSfx) {
                    Text(
                        text = stringResource(
                            id = if (uiState.sfxEnabled) {
                                R.string.game_action_sfx_on
                            } else {
                                R.string.game_action_sfx_off
                            },
                        ),
                    )
                }

                OutlinedButton(onClick = onToggleBgm) {
                    Text(
                        text = stringResource(
                            id = if (uiState.bgmEnabled) {
                                R.string.game_action_bgm_on
                            } else {
                                R.string.game_action_bgm_off
                            },
                        ),
                    )
                }

                val isAdReady = uiState.rewardedAdState is RewardedAdState.Ready
                val isAdBusy = uiState.rewardedAdState is RewardedAdState.Loading ||
                    uiState.rewardedAdState is RewardedAdState.Showing
                FilledTonalButton(
                    onClick = if (isAdReady) onWatchRewardedAd else onPreloadRewardedAd,
                    enabled = !isAdBusy,
                ) {
                    Text(
                        text = stringResource(
                            id = when {
                                isAdReady -> R.string.game_action_watch_ad
                                isAdBusy -> R.string.game_action_loading_ad
                                else -> R.string.game_action_load_ad
                            },
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun GameHudBar(
    uiState: GameUiState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.game_label_score, uiState.score),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(id = R.string.game_label_best, uiState.highScore),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(id = R.string.game_label_coins, uiState.coins),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(id = R.string.game_label_level, uiState.level),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

private class BouncingTargetSimulation {
    private var widthPx: Float = 0f
    private var heightPx: Float = 0f

    var radiusPx: Float = DEFAULT_RADIUS_PX
        private set

    private var prevX: Float = 0.5f
    private var prevY: Float = 0.5f
    private var x: Float = 0.5f
    private var y: Float = 0.5f
    private var vx: Float = 0.32f
    private var vy: Float = 0.24f

    fun updateBounds(size: IntSize) {
        widthPx = size.width.toFloat()
        heightPx = size.height.toFloat()
        val minDimension = minOf(widthPx, heightPx)
        if (minDimension > 0f) {
            radiusPx = (minDimension * 0.11f).coerceIn(MIN_RADIUS_PX, MAX_RADIUS_PX)
        }
    }

    fun step(dtSeconds: Float) {
        prevX = x
        prevY = y
        x += vx * dtSeconds
        y += vy * dtSeconds

        val marginX = if (widthPx > 0f) (radiusPx / widthPx).coerceIn(0.05f, 0.45f) else 0.1f
        val marginY = if (heightPx > 0f) (radiusPx / heightPx).coerceIn(0.05f, 0.45f) else 0.1f

        if (x < marginX) {
            x = marginX
            vx = -vx
        } else if (x > 1f - marginX) {
            x = 1f - marginX
            vx = -vx
        }

        if (y < marginY) {
            y = marginY
            vy = -vy
        } else if (y > 1f - marginY) {
            y = 1f - marginY
            vy = -vy
        }
    }

    fun interpolatedCenter(alpha: Float): Offset {
        val drawX = (prevX + (x - prevX) * alpha) * widthPx
        val drawY = (prevY + (y - prevY) * alpha) * heightPx
        return Offset(drawX, drawY)
    }

    fun tryHit(tapOffset: Offset): Boolean {
        if (widthPx <= 0f || heightPx <= 0f) return false
        val centerX = x * widthPx
        val centerY = y * heightPx
        val distance = hypot(tapOffset.x - centerX, tapOffset.y - centerY)
        if (distance <= radiusPx * HIT_SLOP_MULTIPLIER) {
            vx = (-vx * SPEED_BOOST).coerceIn(-MAX_SPEED, MAX_SPEED)
            vy = (vy * SPEED_BOOST).coerceIn(-MAX_SPEED, MAX_SPEED)
            return true
        }
        return false
    }

    private companion object {
        const val DEFAULT_RADIUS_PX = 56f
        const val MIN_RADIUS_PX = 36f
        const val MAX_RADIUS_PX = 84f
        const val HIT_SLOP_MULTIPLIER = 1.25f
        const val SPEED_BOOST = 1.05f
        const val MAX_SPEED = 0.85f
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    InitTheme {
        GameScreen(
            uiState = GameUiState(
                score = 12,
                highScore = 24,
                coins = 150,
                level = 2,
                loopState = GameLoopState.RUNNING,
                sfxEnabled = true,
                bgmEnabled = true,
                rewardedAdState = RewardedAdState.Ready,
            ),
            onTargetHit = {},
            onTogglePause = {},
            onToggleSfx = {},
            onToggleBgm = {},
            onWatchRewardedAd = {},
            onPreloadRewardedAd = {},
        )
    }
}
