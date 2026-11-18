package io.github.amirisback.androidapp.testutil

import io.github.amirisback.androidapp.core.game.audio.GameAudioController

class FakeGameAudioController : GameAudioController {

    val loadedSfx = mutableMapOf<String, Int>()
    val playedSfx = mutableListOf<String>()
    var activeBgmResId: Int? = null
    var isBgmPlaying: Boolean = false
    var sfxEnabled: Boolean = true
        private set
    var bgmEnabled: Boolean = true
        private set
    var sfxVolume: Float = 1f
        private set
    var bgmVolume: Float = 1f
        private set
    var released: Boolean = false
        private set

    override fun loadSfx(soundKey: String, resId: Int) {
        loadedSfx[soundKey] = resId
    }

    override fun playSfx(soundKey: String, volume: Float) {
        if (sfxEnabled) {
            playedSfx.add(soundKey)
        }
    }

    override fun playBgm(resId: Int, looping: Boolean) {
        activeBgmResId = resId
        isBgmPlaying = bgmEnabled
    }

    override fun pauseBgm() {
        isBgmPlaying = false
    }

    override fun resumeBgm() {
        if (bgmEnabled && activeBgmResId != null) {
            isBgmPlaying = true
        }
    }

    override fun stopBgm() {
        activeBgmResId = null
        isBgmPlaying = false
    }

    override fun setSfxEnabled(enabled: Boolean) {
        sfxEnabled = enabled
    }

    override fun setBgmEnabled(enabled: Boolean) {
        bgmEnabled = enabled
        if (!enabled) {
            isBgmPlaying = false
        }
    }

    override fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
    }

    override fun setBgmVolume(volume: Float) {
        bgmVolume = volume.coerceIn(0f, 1f)
    }

    override fun release() {
        released = true
        stopBgm()
    }
}
