package io.github.amirisback.androidapp.domain.usecase

import io.github.amirisback.androidapp.core.common.Resource
import io.github.amirisback.androidapp.domain.model.Area
import io.github.amirisback.androidapp.domain.model.Category
import io.github.amirisback.androidapp.domain.model.Ingredient
import io.github.amirisback.androidapp.domain.model.Meal
import io.github.amirisback.androidapp.domain.model.MealFilter
import io.github.amirisback.androidapp.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MealInteractor @Inject constructor(
    private val repository: MealRepository,
) : MealUseCase {

    override fun searchMeal(nameMeal: String): Flow<Resource<List<Meal>>> =
        repository.searchMeal(nameMeal)

    override fun listAllMeal(firstLetter: String): Flow<Resource<List<Meal>>> =
        repository.listAllMeal(firstLetter)

    override fun lookupFullMeal(idMeal: String): Flow<Resource<List<Meal>>> =
        repository.lookupFullMeal(idMeal)

    override fun lookupRandomMeal(): Flow<Resource<List<Meal>>> =
        repository.lookupRandomMeal()

    override fun listMealCategories(): Flow<Resource<List<Category>>> =
        repository.listMealCategories()

    override fun listAllCategories(query: String): Flow<Resource<List<Category>>> =
        repository.listAllCategories(query)

    override fun listAllArea(query: String): Flow<Resource<List<Area>>> =
        repository.listAllArea(query)

    override fun listAllIngredients(query: String): Flow<Resource<List<Ingredient>>> =
        repository.listAllIngredients(query)

    override fun filterByIngredient(ingredient: String): Flow<Resource<List<MealFilter>>> =
        repository.filterByIngredient(ingredient)

    override fun filterByCategory(category: String): Flow<Resource<List<MealFilter>>> =
        repository.filterByCategory(category)

    override fun filterByArea(area: String): Flow<Resource<List<MealFilter>>> =
        repository.filterByArea(area)

    override fun insertToFavorite(data: Meal): Flow<Resource<Meal>> =
        repository.insertToFavorite(data)

    override fun getAllFavorite(): Flow<Resource<List<Meal>>> =
        repository.getAllFavorite()

    override fun searchById(idMeal: String): Flow<Resource<List<Meal>>> =
        repository.searchById(idMeal)

    override fun deleteFromTableId(tableId: Int): Flow<Resource<String>> =
        repository.deleteFromTableId(tableId)

    override fun deleteFromMealId(mealId: String): Flow<Resource<String>> =
        repository.deleteFromMealId(mealId)

    override fun nukeData(): Flow<Resource<String>> =
        repository.nukeData()
}
