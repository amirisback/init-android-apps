package io.github.amirisback.androidapp.feature.main

import io.github.amirisback.androidapp.domain.model.Meal

sealed interface MainUiState {
    data object Loading : MainUiState
    data object Empty : MainUiState
    data class Success(val meals: List<Meal>) : MainUiState
    data class Error(val message: String) : MainUiState
}
