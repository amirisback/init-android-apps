package io.github.amirisback.androidapp.ui.features.main

import io.github.amirisback.androidapp.domain.model.MealModel

sealed interface MainUiState {
    object Loading : MainUiState
    data class Success(val meals: List<MealModel>) : MainUiState
    data class Error(val message: String) : MainUiState
}
