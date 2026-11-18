package io.github.amirisback.androidapp.core.game.save

import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreGameSaveStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `persists highScore monotonically, clamps coins and level, stores audio and custom json, and clears`() =
        runTest {
            val testDispatcher = UnconfinedTestDispatcher(testScheduler)
            val dataStore = PreferenceDataStoreFactory.create(
                storage = OkioStorage(
                    fileSystem = FileSystem.SYSTEM,
                    serializer = PreferencesSerializer,
                    producePath = {
                        File(tempFolder.root, "game_save_test.preferences_pb").toOkioPath()
                    },
                ),
                scope = backgroundScope,
            )
            val store = DataStoreGameSaveStore(
                dataStore = dataStore,
                ioDispatcher = testDispatcher,
            )

            assertEquals(GameSaveSnapshot(), store.snapshot.first())

            store.updateHighScore(42)
            assertEquals(42, store.snapshot.first().highScore)

            // Lower score must not overwrite existing highScore
            store.updateHighScore(15)
            assertEquals(42, store.snapshot.first().highScore)

            store.addCoins(100)
            store.addCoins(-30)
            assertEquals(70, store.snapshot.first().coins)

            // Coins never drop below 0
            store.addCoins(-200)
            assertEquals(0, store.snapshot.first().coins)

            store.setCurrentLevel(5)
            assertEquals(5, store.snapshot.first().currentLevel)

            // Level coerces to at least 1
            store.setCurrentLevel(0)
            assertEquals(1, store.snapshot.first().currentLevel)

            store.setAudioSettings(sfxEnabled = false, bgmEnabled = false)
            val audioUpdated = store.snapshot.first()
            assertFalse(audioUpdated.sfxEnabled)
            assertFalse(audioUpdated.bgmEnabled)

            val payload = """{"board":[1,0,2]}"""
            store.saveCustomStateJson(payload)
            assertEquals(payload, store.snapshot.first().customStateJson)

            store.clear()
            val reset = store.snapshot.first()
            assertEquals(GameSaveSnapshot(), reset)
            assertTrue(reset.sfxEnabled)
            assertTrue(reset.bgmEnabled)
        }

    @Test
    fun `addCoins saturates at Int MAX_VALUE and clamps underflow to zero`() = runTest {
        val testDispatcher = UnconfinedTestDispatcher(testScheduler)
        val dataStore = PreferenceDataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = PreferencesSerializer,
                producePath = {
                    File(tempFolder.root, "game_save_overflow_test.preferences_pb").toOkioPath()
                },
            ),
            scope = backgroundScope,
        )
        val store = DataStoreGameSaveStore(
            dataStore = dataStore,
            ioDispatcher = testDispatcher,
        )

        store.addCoins(Int.MAX_VALUE - 5)
        store.addCoins(100)
        assertEquals(Int.MAX_VALUE, store.snapshot.first().coins)

        store.addCoins(Int.MIN_VALUE)
        assertEquals(0, store.snapshot.first().coins)
    }
}
