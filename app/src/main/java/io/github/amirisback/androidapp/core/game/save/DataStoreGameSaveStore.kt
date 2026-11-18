package io.github.amirisback.androidapp.core.game.save

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.amirisback.androidapp.core.di.IoDispatcher
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreGameSaveStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GameSaveStore {

    override val snapshot: Flow<GameSaveSnapshot> = dataStore.data
        .catch { throwable ->
            if (throwable is CancellationException) throw throwable
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { prefs ->
            GameSaveSnapshot(
                highScore = prefs[KEY_HIGH_SCORE] ?: 0,
                currentLevel = prefs[KEY_CURRENT_LEVEL] ?: 1,
                coins = prefs[KEY_COINS] ?: 0,
                sfxEnabled = prefs[KEY_SFX_ENABLED] ?: true,
                bgmEnabled = prefs[KEY_BGM_ENABLED] ?: true,
                customStateJson = prefs[KEY_CUSTOM_STATE_JSON] ?: "",
            )
        }
        .flowOn(ioDispatcher)

    override suspend fun updateHighScore(score: Int) {
        safeEdit { prefs ->
            val current = prefs[KEY_HIGH_SCORE] ?: 0
            if (score > current) {
                prefs[KEY_HIGH_SCORE] = score
            }
        }
    }

    override suspend fun setCurrentLevel(level: Int) {
        safeEdit { prefs ->
            prefs[KEY_CURRENT_LEVEL] = level.coerceAtLeast(1)
        }
    }

    override suspend fun addCoins(delta: Int) {
        safeEdit { prefs ->
            val current = prefs[KEY_COINS] ?: 0
            prefs[KEY_COINS] = (current.toLong() + delta.toLong())
                .coerceIn(0L, Int.MAX_VALUE.toLong())
                .toInt()
        }
    }

    override suspend fun setAudioSettings(sfxEnabled: Boolean, bgmEnabled: Boolean) {
        safeEdit { prefs ->
            prefs[KEY_SFX_ENABLED] = sfxEnabled
            prefs[KEY_BGM_ENABLED] = bgmEnabled
        }
    }

    override suspend fun saveCustomStateJson(json: String) {
        safeEdit { prefs ->
            prefs[KEY_CUSTOM_STATE_JSON] = json
        }
    }

    override suspend fun clear() {
        safeEdit { prefs ->
            prefs.clear()
        }
    }

    private suspend fun safeEdit(transform: (MutablePreferences) -> Unit) {
        withContext(ioDispatcher) {
            try {
                dataStore.edit { prefs ->
                    transform(prefs)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: IOException) {
                // Keep gameplay alive when disk writes fail (e.g., storage full).
            }
        }
    }

    private companion object {
        val KEY_HIGH_SCORE = intPreferencesKey("high_score")
        val KEY_CURRENT_LEVEL = intPreferencesKey("current_level")
        val KEY_COINS = intPreferencesKey("coins")
        val KEY_SFX_ENABLED = booleanPreferencesKey("sfx_enabled")
        val KEY_BGM_ENABLED = booleanPreferencesKey("bgm_enabled")
        val KEY_CUSTOM_STATE_JSON = stringPreferencesKey("custom_state_json")
    }
}
