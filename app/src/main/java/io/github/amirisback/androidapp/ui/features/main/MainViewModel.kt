package io.github.amirisback.androidapp.ui.features.main

import androidx.lifecycle.viewModelScope
import io.github.amirisback.androidapp.common.base.BaseViewModel
import io.github.amirisback.androidapp.common.callback.Resource
import io.github.amirisback.androidapp.domain.source.meal.usecase.MealUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val useCase: MealUseCase,
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun searchMeal(name: String = "") {
        useCase.searchMeal(nameMeal = name)
            .onEach { resource ->
                _uiState.value = when (resource) {
                    is Resource.Loading -> MainUiState.Loading
                    is Resource.Success -> MainUiState.Success(resource.data ?: emptyList())
                    is Resource.Error -> MainUiState.Error(resource.message ?: "Unknown Error")
                }
            }
            .launchIn(viewModelScope)
    }
}
