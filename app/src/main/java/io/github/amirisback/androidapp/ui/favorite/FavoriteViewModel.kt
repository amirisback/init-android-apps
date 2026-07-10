package io.github.amirisback.androidapp.ui.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.amirisback.androidapp.common.base.BaseViewModel
import io.github.amirisback.androidapp.common.callback.Resource
import io.github.amirisback.androidapp.domain.model.MealModel
import io.github.amirisback.androidapp.domain.source.meal.usecase.MealUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val useCase: MealUseCase,
) : BaseViewModel() {

    private var _mealsState = MutableLiveData<Resource<List<MealModel>>>()
    var mealsState: LiveData<Resource<List<MealModel>>> = _mealsState

    fun getData() {
        viewModelScope.launch {
            useCase.getAllFavorite().onEach {
                _mealsState.postValue(it)
            }.launchIn(viewModelScope)
        }
    }

}