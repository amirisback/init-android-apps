package io.github.amirisback.androidapp.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.amirisback.androidapp.core.common.Resource
import io.github.amirisback.androidapp.domain.model.Meal
import io.github.amirisback.androidapp.domain.usecase.MealUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val useCase: MealUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun setMeal(meal: Meal) {
        _uiState.update { it.copy(meal = meal) }
        checkFavoriteStatus(meal.id)
    }

    private fun checkFavoriteStatus(mealId: String) {
        if (mealId.isBlank()) return
        useCase.searchById(mealId)
            .onEach { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isFavorite = resource.data.isNotEmpty(),
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = resource.message,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun addToFavorites() {
        val current = _uiState.value
        val meal = current.meal ?: return
        if (current.isLoading) return

        useCase.insertToFavorite(meal)
            .onEach { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isFavorite = true,
                            userMessage = resource.data.name,
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = resource.message,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun removeFromFavorites() {
        val current = _uiState.value
        val mealId = current.meal?.id ?: return
        if (current.isLoading || mealId.isBlank()) return

        useCase.deleteFromMealId(mealId)
            .onEach { resource ->
                when (resource) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isFavorite = false,
                            isDeleted = true,
                        )
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = resource.message,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun consumeUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
