package io.github.amirisback.androidapp.feature.main

import io.github.amirisback.androidapp.core.common.Resource
import io.github.amirisback.androidapp.domain.model.Meal
import io.github.amirisback.androidapp.testutil.FakeMealUseCase
import io.github.amirisback.androidapp.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `init triggers default Cream search and emits Success when meals returned`() {
        val sampleMeals = listOf(Meal(id = "1", name = "Creamy Chicken Pasta"))
        val useCase = FakeMealUseCase().apply {
            searchMealFlow = { flowOf(Resource.Loading, Resource.Success(sampleMeals)) }
        }

        val viewModel = MainViewModel(useCase)

        assertEquals(listOf(MainViewModel.DEFAULT_QUERY), useCase.searchedQueries)
        assertEquals(MainUiState.Success(sampleMeals), viewModel.uiState.value)
    }

    @Test
    fun `searchMeal emits Empty when repository returns empty list`() {
        val useCase = FakeMealUseCase().apply {
            searchMealFlow = { flowOf(Resource.Loading, Resource.Success(emptyList())) }
        }

        val viewModel = MainViewModel(useCase)

        assertEquals(MainUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `searchMeal emits Error when repository returns Resource Error`() {
        val useCase = FakeMealUseCase().apply {
            searchMealFlow = { flowOf(Resource.Loading, Resource.Error("Network timeout")) }
        }

        val viewModel = MainViewModel(useCase)

        assertEquals(MainUiState.Error("Network timeout"), viewModel.uiState.value)
    }

    @Test
    fun `subsequent searchMeal updates uiState with new query results`() {
        val beefMeals = listOf(Meal(id = "2", name = "Beef Rendang"))
        val useCase = FakeMealUseCase().apply {
            searchMealFlow = { query ->
                if (query == "Beef") {
                    flowOf(Resource.Loading, Resource.Success(beefMeals))
                } else {
                    flowOf(Resource.Loading, Resource.Success(emptyList()))
                }
            }
        }

        val viewModel = MainViewModel(useCase)
        assertEquals(MainUiState.Empty, viewModel.uiState.value)

        viewModel.searchMeal("Beef")
        assertEquals(MainUiState.Success(beefMeals), viewModel.uiState.value)
    }
}
