package io.github.amirisback.androidapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.amirisback.androidapp.domain.source.meal.repository.MealRepository
import io.github.amirisback.androidapp.domain.source.meal.repository.MealRepositoryImpl

/**
 * Created by faisalamircs on 09/09/2025
 * -----------------------------------------
 * Name     : Muhammad Faisal Amir
 * E-mail   : faisalamircs@gmail.com
 * Github   : github.com/amirisback
 * -----------------------------------------
 */


@Module(includes = [NetworkModule::class, DatabaseModule::class])
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideMealRepository(repository: MealRepositoryImpl): MealRepository

}