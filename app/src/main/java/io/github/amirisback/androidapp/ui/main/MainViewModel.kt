package io.github.amirisback.androidapp.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import io.github.amirisback.androidapp.common.base.BaseViewModel
import io.github.amirisback.androidapp.common.callback.Resource
import io.github.amirisback.androidapp.domain.source.meal.usecase.MealUseCase
import io.github.amirisback.androidapp.domain.model.MealModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by Faisal Amir
 * -----------------------------------------
 * Copyright (C) 28/04/2020.
 * All rights reserved
 * -----------------------------------------
 * Name     : Muhammad Faisal Amir
 * E-mail   : faisalamircs@gmail.com
 * Github   : github.com/amirisback
 * -----------------------------------------
 * Frogobox Inc
 *
 */

@HiltViewModel
class MainViewModel @Inject constructor(
    private val useCase: MealUseCase,
) : BaseViewModel() {

    private var _mealsState = MutableLiveData<Resource<List<MealModel>>>()
    var mealsState: LiveData<Resource<List<MealModel>>> = _mealsState

    fun searchMeal(name: String = "") {
        viewModelScope.launch {
            useCase.searchMeal(
                nameMeal = name
            ).onEach {
                _mealsState.postValue(it)
            }.launchIn(viewModelScope)
        }
    }

}