package io.github.amirisback.androidapp.feature.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.amirisback.androidapp.core.common.Resource
import io.github.amirisback.androidapp.domain.usecase.MealUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val useCase: MealUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoriteUiState>(FavoriteUiState.Loading)
    val uiState: StateFlow<FavoriteUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun loadFavorites() {
        loadJob?.cancel()
        loadJob = useCase.getAllFavorite()
            .onEach { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> FavoriteUiState.Loading
                    is Resource.Success -> {
                        if (resource.data.isEmpty()) {
                            FavoriteUiState.Empty
                        } else {
                            FavoriteUiState.Success(resource.data)
                        }
                    }
                    is Resource.Error -> FavoriteUiState.Error(resource.message.ifBlank { "Unknown error" })
                }
            }
            .launchIn(viewModelScope)
    }
}
