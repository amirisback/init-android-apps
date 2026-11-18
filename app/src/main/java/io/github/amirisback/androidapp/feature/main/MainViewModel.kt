package io.github.amirisback.androidapp.feature.main

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
class MainViewModel @Inject constructor(
    private val useCase: MealUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        searchMeal(DEFAULT_QUERY)
    }

    fun searchMeal(name: String = DEFAULT_QUERY) {
        searchJob?.cancel()
        searchJob = useCase.searchMeal(nameMeal = name)
            .onEach { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> MainUiState.Loading
                    is Resource.Success -> {
                        if (resource.data.isEmpty()) {
                            MainUiState.Empty
                        } else {
                            MainUiState.Success(resource.data)
                        }
                    }
                    is Resource.Error -> MainUiState.Error(resource.message.ifBlank { "Unknown error" })
                }
            }
            .launchIn(viewModelScope)
    }

    companion object {
        const val DEFAULT_QUERY = "Cream"
    }
}
