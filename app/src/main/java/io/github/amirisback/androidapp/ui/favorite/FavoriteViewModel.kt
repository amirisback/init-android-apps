package io.github.amirisback.androidapp.ui.favorite

import androidx.lifecycle.viewModelScope
import io.github.amirisback.androidapp.common.base.BaseViewModel
import io.github.amirisback.androidapp.common.callback.Resource
import io.github.amirisback.androidapp.domain.model.MealModel
import io.github.amirisback.androidapp.domain.source.meal.usecase.MealUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val useCase: MealUseCase,
) : BaseViewModel() {

    private val _mealsState = MutableStateFlow<Resource<List<MealModel>>>(Resource.Loading())
    val mealsState: StateFlow<Resource<List<MealModel>>> = _mealsState.asStateFlow()

    fun getData() {
        useCase.getAllFavorite()
            .onEach {
                _mealsState.value = it
            }
            .launchIn(viewModelScope)
    }

}