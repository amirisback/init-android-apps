package io.github.amirisback.androidapp.feature.favorite

import io.github.amirisback.androidapp.core.common.Resource
import io.github.amirisback.androidapp.domain.model.Meal
import io.github.amirisback.androidapp.testutil.FakeMealUseCase
import io.github.amirisback.androidapp.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FavoriteViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is Loading before loadFavorites is called`() {
        val viewModel = FavoriteViewModel(FakeMealUseCase())
        assertEquals(FavoriteUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `loadFavorites emits Empty when no favorites stored`() {
        val useCase = FakeMealUseCase().apply {
            getAllFavoriteFlow = { flowOf(Resource.Loading, Resource.Success(emptyList())) }
        }
        val viewModel = FavoriteViewModel(useCase)

        viewModel.loadFavorites()

        assertEquals(FavoriteUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `loadFavorites emits Success when favorites exist`() {
        val favorites = listOf(Meal(id = "10", name = "Nasi Goreng"))
        val useCase = FakeMealUseCase().apply {
            getAllFavoriteFlow = { flowOf(Resource.Loading, Resource.Success(favorites)) }
        }
        val viewModel = FavoriteViewModel(useCase)

        viewModel.loadFavorites()

        assertEquals(FavoriteUiState.Success(favorites), viewModel.uiState.value)
    }

    @Test
    fun `loadFavorites emits Error when database query fails`() {
        val useCase = FakeMealUseCase().apply {
            getAllFavoriteFlow = { flowOf(Resource.Loading, Resource.Error("Read failed")) }
        }
        val viewModel = FavoriteViewModel(useCase)

        viewModel.loadFavorites()

        assertEquals(FavoriteUiState.Error("Read failed"), viewModel.uiState.value)
    }
}
