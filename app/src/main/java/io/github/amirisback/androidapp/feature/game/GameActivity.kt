package io.github.amirisback.androidapp.feature.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.frogobox.compose.view.FrogoComposeActivity
import com.frogobox.sdk.ext.showToast
import dagger.hilt.android.AndroidEntryPoint
import io.github.amirisback.androidapp.core.game.ads.RewardedAdController
import io.github.amirisback.androidapp.core.game.audio.GameAudioController
import io.github.amirisback.androidapp.core.ui.theme.InitTheme
import javax.inject.Inject

@AndroidEntryPoint
class GameActivity : FrogoComposeActivity() {

    @Inject
    lateinit var audioController: GameAudioController

    @Inject
    lateinit var rewardedAdController: RewardedAdController

    private val viewModel: GameViewModel by viewModels()

    override fun onCreateExt(savedInstanceState: Bundle?) {
        super.onCreateExt(savedInstanceState)
        enableEdgeToEdge()
    }

    @Composable
    override fun SetupCompose() {
        InitTheme {
            GameRoute(
                viewModel = viewModel,
                audioController = audioController,
                onShowRewardedAd = {
                    rewardedAdController.show(
                        activity = this,
                        onRewardEarned = viewModel::onRewardEarned,
                    )
                },
                onShowMessage = { message -> showToast(message) },
            )
        }
    }

    companion object {
        fun createIntent(context: Context): Intent = Intent(context, GameActivity::class.java)

        fun launch(context: Context) {
            context.startActivity(createIntent(context))
        }
    }
}
