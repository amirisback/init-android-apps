package io.github.amirisback.androidapp.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.amirisback.androidapp.core.game.ads.AdMobRewardedAdController
import io.github.amirisback.androidapp.core.game.ads.RewardedAdController
import io.github.amirisback.androidapp.core.game.audio.AndroidGameAudioManager
import io.github.amirisback.androidapp.core.game.audio.GameAudioController
import io.github.amirisback.androidapp.core.game.save.DataStoreGameSaveStore
import io.github.amirisback.androidapp.core.game.save.GameSaveStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GameModule {

    @Binds
    @Singleton
    abstract fun bindGameSaveStore(impl: DataStoreGameSaveStore): GameSaveStore

    @Binds
    @Singleton
    abstract fun bindGameAudioController(impl: AndroidGameAudioManager): GameAudioController

    @Binds
    @Singleton
    abstract fun bindRewardedAdController(impl: AdMobRewardedAdController): RewardedAdController

    companion object {
        private const val GAME_SAVE_FILE_NAME = "game_save"

        @Provides
        @Singleton
        fun provideGameSaveDataStore(
            @ApplicationContext context: Context,
        ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        ) {
            context.preferencesDataStoreFile(GAME_SAVE_FILE_NAME)
        }
    }
}
