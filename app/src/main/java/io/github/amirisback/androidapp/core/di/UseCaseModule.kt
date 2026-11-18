package io.github.amirisback.androidapp.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import io.github.amirisback.androidapp.domain.usecase.MealInteractor
import io.github.amirisback.androidapp.domain.usecase.MealUseCase

@Module
@InstallIn(ViewModelComponent::class)
abstract class UseCaseModule {

    @Binds
    @ViewModelScoped
    abstract fun bindMealUseCase(impl: MealInteractor): MealUseCase
}
