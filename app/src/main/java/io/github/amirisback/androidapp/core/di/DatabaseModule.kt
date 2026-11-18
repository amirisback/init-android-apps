package io.github.amirisback.androidapp.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.amirisback.androidapp.core.common.appDatabaseName
import io.github.amirisback.androidapp.core.common.appIsDebug
import io.github.amirisback.androidapp.data.local.dao.MealDao
import io.github.amirisback.androidapp.data.local.database.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val builder = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            appDatabaseName,
        )
        if (appIsDebug) {
            builder.fallbackToDestructiveMigration(dropAllTables = true)
        }
        return builder.build()
    }

    @Provides
    fun provideMealDao(database: AppDatabase): MealDao = database.mealDao()
}
