package io.github.amirisback.androidapp.core.game.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.amirisback.androidapp.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobRewardedAdController @Inject constructor(
    @ApplicationContext private val context: Context,
) : RewardedAdController {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val initialized = AtomicBoolean(false)
    private val _state = MutableStateFlow<RewardedAdState>(RewardedAdState.Idle)
    override val state: StateFlow<RewardedAdState> = _state.asStateFlow()

    private var rewardedAd: RewardedAd? = null

    override fun preload() {
        runOnMainThread {
            preloadOnMainThread()
        }
    }

    override fun show(
        activity: Activity,
        onRewardEarned: (GameReward) -> Unit,
        onDismissed: () -> Unit,
    ) {
        runOnMainThread {
            if (activity.isFinishing || activity.isDestroyed) {
                return@runOnMainThread
            }

            val ad = rewardedAd
            if (ad == null) {
                if (_state.value !is RewardedAdState.Loading &&
                    _state.value !is RewardedAdState.Failed
                ) {
                    preloadOnMainThread()
                }
                return@runOnMainThread
            }

            rewardedAd = null
            _state.value = RewardedAdState.Showing

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    _state.value = RewardedAdState.Showing
                }

                override fun onAdDismissedFullScreenContent() {
                    ad.fullScreenContentCallback = null
                    _state.value = RewardedAdState.Idle
                    onDismissed()
                    preloadOnMainThread()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    ad.fullScreenContentCallback = null
                    _state.value = RewardedAdState.Failed(adError.message)
                    onDismissed()
                }
            }

            ad.show(activity) { rewardItem ->
                onRewardEarned(
                    GameReward(
                        type = rewardItem.type,
                        amount = rewardItem.amount,
                    ),
                )
            }
        }
    }

    private fun preloadOnMainThread() {
        val appId = context.getString(R.string.admob_app_id).trim()
        val adUnitId = context.getString(R.string.admob_rewarded_id).trim()
        if (appId.isBlank() || adUnitId.isBlank()) {
            _state.value = RewardedAdState.Failed("AdMob App ID or Rewarded Ad Unit ID is not configured")
            return
        }

        if (_state.value is RewardedAdState.Loading || rewardedAd != null) {
            return
        }

        if (initialized.compareAndSet(false, true)) {
            MobileAds.initialize(context)
        }

        _state.value = RewardedAdState.Loading
        RewardedAd.load(
            context,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    _state.value = RewardedAdState.Ready
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardedAd = null
                    _state.value = RewardedAdState.Failed(loadAdError.message)
                }
            },
        )
    }

    private inline fun runOnMainThread(crossinline block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post { block() }
        }
    }
}
