package io.github.amirisback.androidapp.ui.detail

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
class DetailViewModel @Inject constructor(
    private val useCase: MealUseCase,
) : BaseViewModel() {

    var mealModel: MealModel? = null

    private val _mealsState = MutableStateFlow<Resource<List<MealModel>>>(Resource.Loading())
    val mealsState: StateFlow<Resource<List<MealModel>>> = _mealsState.asStateFlow()

    private val _insertState = MutableStateFlow<Resource<MealModel>?>(null)
    val insertState: StateFlow<Resource<MealModel>?> = _insertState.asStateFlow()

    private val _deleteState = MutableStateFlow<Resource<String>?>(null)
    val deleteState: StateFlow<Resource<String>?> = _deleteState.asStateFlow()

    fun getData() {
        mealModel?.idMeal?.let { id ->
            useCase.searchById(id)
                .onEach { _mealsState.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun insertToDB() {
        mealModel?.let { model ->
            useCase.insertToFavorite(model)
                .onEach { _insertState.value = it }
                .launchIn(viewModelScope)
        }
    }

    fun removeFromDb() {
        mealModel?.idMeal?.let { id ->
            useCase.deleteFromMealId(id)
                .onEach { _deleteState.value = it }
                .launchIn(viewModelScope)
        }
    }

}