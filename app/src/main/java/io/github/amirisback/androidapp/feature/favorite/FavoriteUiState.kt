package io.github.amirisback.androidapp.feature.favorite

import io.github.amirisback.androidapp.domain.model.Meal

sealed interface FavoriteUiState {
    data object Loading : FavoriteUiState
    data object Empty : FavoriteUiState
    data class Success(val meals: List<Meal>) : FavoriteUiState
    data class Error(val message: String) : FavoriteUiState
}
