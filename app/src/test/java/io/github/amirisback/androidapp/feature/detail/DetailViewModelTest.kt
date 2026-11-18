package io.github.amirisback.androidapp.feature.detail

import io.github.amirisback.androidapp.core.common.Resource
import io.github.amirisback.androidapp.domain.model.Meal
import io.github.amirisback.androidapp.testutil.FakeMealUseCase
import io.github.amirisback.androidapp.testutil.MainDispatcherRule
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val sampleMeal = Meal(
        id = "52772",
        name = "Teriyaki Chicken",
        category = "Chicken",
        area = "Japanese",
    )

    @Test
    fun `setMeal checks favorite status and marks isFavorite true when found`() {
        val useCase = FakeMealUseCase().apply {
            searchByIdFlow = { flowOf(Resource.Loading, Resource.Success(listOf(sampleMeal))) }
        }
        val viewModel = DetailViewModel(useCase)

        viewModel.setMeal(sampleMeal)

        val state = viewModel.uiState.value
        assertEquals(sampleMeal, state.meal)
        assertTrue(state.isFavorite)
        assertFalse(state.isLoading)
    }

    @Test
    fun `addToFavorites marks meal as favorite and emits consumable userMessage`() {
        val useCase = FakeMealUseCase().apply {
            searchByIdFlow = { flowOf(Resource.Success(emptyList())) }
            insertToFavoriteFlow = { meal -> flowOf(Resource.Loading, Resource.Success(meal)) }
        }
        val viewModel = DetailViewModel(useCase)
        viewModel.setMeal(sampleMeal)

        viewModel.addToFavorites()

        assertTrue(viewModel.uiState.value.isFavorite)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(sampleMeal.name, viewModel.uiState.value.userMessage)

        viewModel.consumeUserMessage()
        assertNull(viewModel.uiState.value.userMessage)
    }

    @Test
    fun `removeFromFavorites clears favorite flag and sets isDeleted true`() {
        val useCase = FakeMealUseCase().apply {
            searchByIdFlow = { flowOf(Resource.Success(listOf(sampleMeal))) }
            deleteFromMealIdFlow = { flowOf(Resource.Loading, Resource.Success("Deleted")) }
        }
        val viewModel = DetailViewModel(useCase)
        viewModel.setMeal(sampleMeal)

        viewModel.removeFromFavorites()

        val state = viewModel.uiState.value
        assertFalse(state.isFavorite)
        assertFalse(state.isLoading)
        assertTrue(state.isDeleted)
    }

    @Test
    fun `loading guard prevents duplicate addToFavorites calls while operation is in flight`() {
        val insertStream = MutableSharedFlow<Resource<Meal>>(replay = 1)
        val useCase = FakeMealUseCase().apply {
            searchByIdFlow = { flowOf(Resource.Success(emptyList())) }
            insertToFavoriteFlow = { insertStream }
        }
        val viewModel = DetailViewModel(useCase)
        viewModel.setMeal(sampleMeal)

        insertStream.tryEmit(Resource.Loading)
        viewModel.addToFavorites()
        assertTrue(viewModel.uiState.value.isLoading)

        viewModel.addToFavorites()
        assertEquals(1, useCase.insertCallCount)
    }

    @Test
    fun `error during favorite operation populates consumable errorMessage`() {
        val useCase = FakeMealUseCase().apply {
            searchByIdFlow = { flowOf(Resource.Success(emptyList())) }
            insertToFavoriteFlow = { flowOf(Resource.Loading, Resource.Error("Disk full")) }
        }
        val viewModel = DetailViewModel(useCase)
        viewModel.setMeal(sampleMeal)

        viewModel.addToFavorites()

        assertEquals("Disk full", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.consumeErrorMessage()
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
