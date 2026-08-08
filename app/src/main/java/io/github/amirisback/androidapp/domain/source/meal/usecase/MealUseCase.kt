package io.github.amirisback.androidapp.domain.source.meal.usecase

import io.github.amirisback.androidapp.common.callback.Resource
import io.github.amirisback.androidapp.domain.model.AreaModel
import io.github.amirisback.androidapp.domain.model.CategoryModel
import io.github.amirisback.androidapp.domain.model.IngredientModel
import io.github.amirisback.androidapp.domain.model.MealFilterModel
import io.github.amirisback.androidapp.domain.model.MealModel
import kotlinx.coroutines.flow.Flow

/**
 * Created by faisalamircs on 09/09/2025
 * -----------------------------------------
 * Name     : Muhammad Faisal Amir
 * E-mail   : faisalamircs@gmail.com
 * Github   : github.com/amirisback
 * -----------------------------------------
 */


interface MealUseCase {

    // Search meal by name
    fun searchMeal(nameMeal: String): Flow<Resource<List<MealModel>>>

    // List all meals by first letter
    fun listAllMeal(firstLetter: String): Flow<Resource<List<MealModel>>>

    // Lookup full meal details by id
    fun lookupFullMeal(idMeal: String): Flow<Resource<List<MealModel>>>

    // Lookup a single random meal
    fun lookupRandomMeal(): Flow<Resource<List<MealModel>>>

    // List all meal categories
    fun listMealCategories(): Flow<Resource<List<CategoryModel>>>

    // List all Categories
    fun listAllCategories(query: String): Flow<Resource<List<CategoryModel>>>

    // List all Area
    fun listAllArea(query: String): Flow<Resource<List<AreaModel>>>

    // List all Ingredients
    fun listAllIngredients(query: String): Flow<Resource<List<IngredientModel>>>

    // Filter by main ingredient
    fun filterByIngredient(ingredient: String): Flow<Resource<List<MealFilterModel>>>

    // Filter by Category
    fun filterByCategory(category: String): Flow<Resource<List<MealFilterModel>>>

    // Filter by Area
    fun filterByArea(area: String): Flow<Resource<List<MealFilterModel>>>

    fun insertToFavorite(data: MealModel): Flow<Resource<MealModel>>

    fun getAllFavorite(): Flow<Resource<List<MealModel>>>

    fun searchById(idMeal : String): Flow<Resource<List<MealModel>>>

    fun deleteFromTableId(tableId: Int): Flow<Resource<String>>

    fun deleteFromMealId(mealId: String): Flow<Resource<String>>

    fun nukeData(): Flow<Resource<String>>

}