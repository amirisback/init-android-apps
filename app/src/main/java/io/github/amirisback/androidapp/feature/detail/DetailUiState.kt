package io.github.amirisback.androidapp.feature.detail

import io.github.amirisback.androidapp.domain.model.Meal

data class DetailUiState(
    val meal: Meal? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null,
    val userMessage: String? = null,
    val isDeleted: Boolean = false,
)
