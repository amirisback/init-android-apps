package io.github.amirisback.androidapp.core.game.audio

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.LifecycleResumeEffect

interface GameAudioController {
    fun loadSfx(soundKey: String, @RawRes resId: Int)
    fun playSfx(soundKey: String, volume: Float = 1f)
    fun playBgm(@RawRes resId: Int, looping: Boolean = true)
    fun pauseBgm()
    fun resumeBgm()
    fun onLifecyclePause() = pauseBgm()
    fun onLifecycleResume() = resumeBgm()
    fun stopBgm()
    fun setSfxEnabled(enabled: Boolean)
    fun setBgmEnabled(enabled: Boolean)
    fun setSfxVolume(volume: Float)
    fun setBgmVolume(volume: Float)
    fun release()
}

@Composable
fun GameAudioLifecycleEffect(audioController: GameAudioController) {
    LifecycleResumeEffect(audioController) {
        audioController.onLifecycleResume()
        onPauseOrDispose {
            audioController.onLifecyclePause()
        }
    }
}
