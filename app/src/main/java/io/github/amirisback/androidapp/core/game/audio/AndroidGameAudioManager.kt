package io.github.amirisback.androidapp.core.game.audio

import android.content.Context
import android.content.res.Resources
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.annotation.RawRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidGameAudioManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : GameAudioController {

    private val sfxLock = Any()
    private val bgmLock = Any()

    private val sfxResIds = HashMap<String, Int>()
    private val sfxSoundIds = HashMap<String, Int>()
    private val loadedSoundIds = HashSet<Int>()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(MAX_SFX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
        .apply {
            setOnLoadCompleteListener { _, sampleId, status ->
                synchronized(sfxLock) {
                    if (!isReleased && status == 0) {
                        loadedSoundIds.add(sampleId)
                    }
                }
            }
        }

    private var mediaPlayer: MediaPlayer? = null
    private var currentBgmResId: Int = 0
    private var currentBgmLooping: Boolean = true
    private var isBgmPausedByUser: Boolean = false
    private var isBgmPausedByLifecycle: Boolean = false

    @Volatile
    private var sfxEnabled: Boolean = true

    @Volatile
    private var bgmEnabled: Boolean = true

    @Volatile
    private var sfxVolume: Float = 1f

    @Volatile
    private var bgmVolume: Float = 1f

    @Volatile
    private var isReleased: Boolean = false

    override fun loadSfx(soundKey: String, @RawRes resId: Int) {
        if (soundKey.isBlank() || resId == 0) return
        synchronized(sfxLock) {
            if (isReleased) return
            if (sfxResIds[soundKey] == resId) return

            sfxSoundIds.remove(soundKey)?.let { previousSoundId ->
                loadedSoundIds.remove(previousSoundId)
                soundPool.unload(previousSoundId)
            }
            sfxResIds.remove(soundKey)

            val soundId = try {
                soundPool.load(context, resId, 1)
            } catch (_: Resources.NotFoundException) {
                0
            } catch (_: IllegalArgumentException) {
                0
            }
            if (soundId > 0) {
                sfxResIds[soundKey] = resId
                sfxSoundIds[soundKey] = soundId
            }
        }
    }

    override fun playSfx(soundKey: String, volume: Float) {
        if (!volume.isFinite()) return
        synchronized(sfxLock) {
            if (isReleased || !sfxEnabled) return
            val soundId = sfxSoundIds[soundKey] ?: return
            if (!loadedSoundIds.contains(soundId)) return
            val effectiveVolume = (volume.coerceIn(0f, 1f) * sfxVolume).coerceIn(0f, 1f)
            soundPool.play(soundId, effectiveVolume, effectiveVolume, 1, 0, 1f)
        }
    }

    override fun playBgm(@RawRes resId: Int, looping: Boolean) {
        if (resId == 0) return
        synchronized(bgmLock) {
            if (isReleased) return
            startBgmLocked(resId, looping)
        }
    }

    override fun pauseBgm() {
        synchronized(bgmLock) {
            if (isReleased) return
            isBgmPausedByUser = true
            isBgmPausedByLifecycle = false
            pauseMediaPlayerLocked()
        }
    }

    override fun resumeBgm() {
        synchronized(bgmLock) {
            if (isReleased) return
            isBgmPausedByUser = false
            isBgmPausedByLifecycle = false
            if (!bgmEnabled) return
            val player = mediaPlayer ?: return
            startPlayerSafelyLocked(player)
        }
    }

    override fun onLifecyclePause() {
        synchronized(bgmLock) {
            if (isReleased) return
            val player = mediaPlayer ?: return
            val wasPlaying = try {
                player.isPlaying
            } catch (_: IllegalStateException) {
                releaseMediaPlayerLocked()
                false
            }
            if (wasPlaying) {
                isBgmPausedByLifecycle = true
                pauseMediaPlayerLocked()
            }
        }
    }

    override fun onLifecycleResume() {
        synchronized(bgmLock) {
            if (isReleased || !isBgmPausedByLifecycle) return
            isBgmPausedByLifecycle = false
            if (!bgmEnabled || isBgmPausedByUser) return
            val player = mediaPlayer ?: return
            startPlayerSafelyLocked(player)
        }
    }

    override fun stopBgm() {
        synchronized(bgmLock) {
            currentBgmResId = 0
            isBgmPausedByUser = false
            isBgmPausedByLifecycle = false
            releaseMediaPlayerLocked()
        }
    }

    override fun setSfxEnabled(enabled: Boolean) {
        synchronized(sfxLock) {
            if (isReleased) return
            sfxEnabled = enabled
        }
    }

    override fun setBgmEnabled(enabled: Boolean) {
        synchronized(bgmLock) {
            if (isReleased) return
            bgmEnabled = enabled
            val player = mediaPlayer
            if (!enabled) {
                pauseMediaPlayerLocked()
            } else if (player != null) {
                if (!isBgmPausedByUser && !isBgmPausedByLifecycle) {
                    startPlayerSafelyLocked(player)
                }
            } else if (currentBgmResId != 0 && !isBgmPausedByUser && !isBgmPausedByLifecycle) {
                startBgmLocked(currentBgmResId, currentBgmLooping)
            }
        }
    }

    override fun setSfxVolume(volume: Float) {
        if (!volume.isFinite()) return
        synchronized(sfxLock) {
            if (isReleased) return
            sfxVolume = volume.coerceIn(0f, 1f)
        }
    }

    override fun setBgmVolume(volume: Float) {
        if (!volume.isFinite()) return
        val clamped = volume.coerceIn(0f, 1f)
        synchronized(bgmLock) {
            if (isReleased) return
            bgmVolume = clamped
            try {
                mediaPlayer?.setVolume(clamped, clamped)
            } catch (_: IllegalStateException) {
                releaseMediaPlayerLocked()
            }
        }
    }

    override fun release() {
        synchronized(bgmLock) {
            if (isReleased) return
            isReleased = true
            currentBgmResId = 0
            isBgmPausedByUser = false
            isBgmPausedByLifecycle = false
            releaseMediaPlayerLocked()
        }
        synchronized(sfxLock) {
            sfxResIds.clear()
            sfxSoundIds.clear()
            loadedSoundIds.clear()
            soundPool.release()
        }
    }

    private fun startBgmLocked(@RawRes resId: Int, looping: Boolean) {
        releaseMediaPlayerLocked()
        val player = try {
            MediaPlayer.create(context, resId)
        } catch (_: Resources.NotFoundException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        } catch (_: IllegalStateException) {
            null
        }
        if (player == null) {
            currentBgmResId = 0
            return
        }

        currentBgmResId = resId
        currentBgmLooping = looping
        isBgmPausedByUser = false
        isBgmPausedByLifecycle = false

        player.isLooping = looping
        player.setVolume(bgmVolume, bgmVolume)
        if (!looping) {
            player.setOnCompletionListener {
                synchronized(bgmLock) {
                    if (mediaPlayer === player) {
                        currentBgmResId = 0
                        isBgmPausedByUser = false
                        isBgmPausedByLifecycle = false
                        releaseMediaPlayerLocked()
                    }
                }
            }
        }

        mediaPlayer = player
        if (bgmEnabled) {
            startPlayerSafelyLocked(player)
        }
    }

    private fun pauseMediaPlayerLocked() {
        val player = mediaPlayer ?: return
        try {
            if (player.isPlaying) {
                player.pause()
            }
        } catch (_: IllegalStateException) {
            releaseMediaPlayerLocked()
        }
    }

    private fun startPlayerSafelyLocked(player: MediaPlayer) {
        try {
            if (!player.isPlaying) {
                player.start()
            }
        } catch (_: IllegalStateException) {
            releaseMediaPlayerLocked()
        }
    }

    private fun releaseMediaPlayerLocked() {
        val player = mediaPlayer ?: return
        mediaPlayer = null
        try {
            if (player.isPlaying) {
                player.stop()
            }
        } catch (_: IllegalStateException) {
            // Player already stopped or uninitialized.
        }
        try {
            player.release()
        } catch (_: IllegalStateException) {
            // Already released.
        }
    }

    private companion object {
        const val MAX_SFX_STREAMS = 8
    }
}
