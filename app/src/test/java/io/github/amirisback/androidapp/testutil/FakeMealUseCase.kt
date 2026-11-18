package io.github.amirisback.androidapp.testutil

import io.github.amirisback.androidapp.core.common.Resource
import io.github.amirisback.androidapp.domain.model.Area
import io.github.amirisback.androidapp.domain.model.Category
import io.github.amirisback.androidapp.domain.model.Ingredient
import io.github.amirisback.androidapp.domain.model.Meal
import io.github.amirisback.androidapp.domain.model.MealFilter
import io.github.amirisback.androidapp.domain.usecase.MealUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeMealUseCase : MealUseCase {

    var searchMealFlow: (String) -> Flow<Resource<List<Meal>>> = {
        flowOf(Resource.Loading, Resource.Success(emptyList()))
    }
    var getAllFavoriteFlow: () -> Flow<Resource<List<Meal>>> = {
        flowOf(Resource.Loading, Resource.Success(emptyList()))
    }
    var searchByIdFlow: (String) -> Flow<Resource<List<Meal>>> = {
        flowOf(Resource.Loading, Resource.Success(emptyList()))
    }
    var insertToFavoriteFlow: (Meal) -> Flow<Resource<Meal>> = { meal ->
        flowOf(Resource.Loading, Resource.Success(meal))
    }
    var deleteFromMealIdFlow: (String) -> Flow<Resource<String>> = {
        flowOf(Resource.Loading, Resource.Success("Deleted favorite item"))
    }

    val searchedQueries = mutableListOf<String>()
    var insertCallCount = 0
    var deleteCallCount = 0

    override fun searchMeal(nameMeal: String): Flow<Resource<List<Meal>>> {
        searchedQueries.add(nameMeal)
        return searchMealFlow(nameMeal)
    }

    override fun listAllMeal(firstLetter: String): Flow<Resource<List<Meal>>> =
        flowOf(Resource.Success(emptyList()))

    override fun lookupFullMeal(idMeal: String): Flow<Resource<List<Meal>>> =
        flowOf(Resource.Success(emptyList()))

    override fun lookupRandomMeal(): Flow<Resource<List<Meal>>> =
        flowOf(Resource.Success(emptyList()))

    override fun listMealCategories(): Flow<Resource<List<Category>>> =
        flowOf(Resource.Success(emptyList()))

    override fun listAllCategories(query: String): Flow<Resource<List<Category>>> =
        flowOf(Resource.Success(emptyList()))

    override fun listAllArea(query: String): Flow<Resource<List<Area>>> =
        flowOf(Resource.Success(emptyList()))

    override fun listAllIngredients(query: String): Flow<Resource<List<Ingredient>>> =
        flowOf(Resource.Success(emptyList()))

    override fun filterByIngredient(ingredient: String): Flow<Resource<List<MealFilter>>> =
        flowOf(Resource.Success(emptyList()))

    override fun filterByCategory(category: String): Flow<Resource<List<MealFilter>>> =
        flowOf(Resource.Success(emptyList()))

    override fun filterByArea(area: String): Flow<Resource<List<MealFilter>>> =
        flowOf(Resource.Success(emptyList()))

    override fun insertToFavorite(data: Meal): Flow<Resource<Meal>> {
        insertCallCount++
        return insertToFavoriteFlow(data)
    }

    override fun getAllFavorite(): Flow<Resource<List<Meal>>> = getAllFavoriteFlow()

    override fun searchById(idMeal: String): Flow<Resource<List<Meal>>> = searchByIdFlow(idMeal)

    override fun deleteFromTableId(tableId: Int): Flow<Resource<String>> =
        flowOf(Resource.Success("Deleted"))

    override fun deleteFromMealId(mealId: String): Flow<Resource<String>> {
        deleteCallCount++
        return deleteFromMealIdFlow(mealId)
    }

    override fun nukeData(): Flow<Resource<String>> =
        flowOf(Resource.Success("Nuked"))
}
