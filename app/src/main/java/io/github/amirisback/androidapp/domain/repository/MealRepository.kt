package io.github.amirisback.androidapp.domain.repository

import io.github.amirisback.androidapp.core.common.Resource
import io.github.amirisback.androidapp.domain.model.Area
import io.github.amirisback.androidapp.domain.model.Category
import io.github.amirisback.androidapp.domain.model.Ingredient
import io.github.amirisback.androidapp.domain.model.Meal
import io.github.amirisback.androidapp.domain.model.MealFilter
import kotlinx.coroutines.flow.Flow

interface MealRepository {

    fun searchMeal(nameMeal: String): Flow<Resource<List<Meal>>>

    fun listAllMeal(firstLetter: String): Flow<Resource<List<Meal>>>

    fun lookupFullMeal(idMeal: String): Flow<Resource<List<Meal>>>

    fun lookupRandomMeal(): Flow<Resource<List<Meal>>>

    fun listMealCategories(): Flow<Resource<List<Category>>>

    fun listAllCategories(query: String): Flow<Resource<List<Category>>>

    fun listAllArea(query: String): Flow<Resource<List<Area>>>

    fun listAllIngredients(query: String): Flow<Resource<List<Ingredient>>>

    fun filterByIngredient(ingredient: String): Flow<Resource<List<MealFilter>>>

    fun filterByCategory(category: String): Flow<Resource<List<MealFilter>>>

    fun filterByArea(area: String): Flow<Resource<List<MealFilter>>>

    fun insertToFavorite(data: Meal): Flow<Resource<Meal>>

    fun getAllFavorite(): Flow<Resource<List<Meal>>>

    fun searchById(idMeal: String): Flow<Resource<List<Meal>>>

    fun deleteFromTableId(tableId: Int): Flow<Resource<String>>

    fun deleteFromMealId(mealId: String): Flow<Resource<String>>

    fun nukeData(): Flow<Resource<String>>
}
