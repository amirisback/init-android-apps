package io.github.amirisback.androidapp.data.remote.api

import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.PATH_API_KEY
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.QUERY_AREA
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.QUERY_CATEGORY
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.QUERY_FIRST_LETTER
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.QUERY_ID
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.QUERY_INGREDIENT
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.QUERY_NAME
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.URL_CATEGORIES
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.URL_FILTER
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.URL_LIST
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.URL_LOOKUP_MEAL
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.URL_RANDOM_MEAL
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig.URL_SEARCH_MEAL
import io.github.amirisback.androidapp.data.remote.dto.AreaDto
import io.github.amirisback.androidapp.data.remote.dto.CategoryDto
import io.github.amirisback.androidapp.data.remote.dto.CategoryResponseDto
import io.github.amirisback.androidapp.data.remote.dto.IngredientDto
import io.github.amirisback.androidapp.data.remote.dto.MealDto
import io.github.amirisback.androidapp.data.remote.dto.MealFilterDto
import io.github.amirisback.androidapp.data.remote.dto.MealResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MealApiService {

    @GET(URL_SEARCH_MEAL)
    suspend fun searchMeal(
        @Path(PATH_API_KEY) apiKey: String,
        @Query(QUERY_NAME) nameMeal: String,
    ): Response<MealResponseDto<MealDto>>

    @GET(URL_SEARCH_MEAL)
    suspend fun listAllMeal(
        @Path(PATH_API_KEY) apiKey: String,
        @Query(QUERY_FIRST_LETTER) firstLetter: String,
    ): Response<MealResponseDto<MealDto>>

    @GET(URL_LOOKUP_MEAL)
    suspend fun lookupFullMeal(
        @Path(PATH_API_KEY) apiKey: String,
        @Query(QUERY_ID) idMeal: String,
    ): Response<MealResponseDto<MealDto>>

    @GET(URL_RANDOM_MEAL)
    suspend fun lookupRandomMeal(
        @Path(PATH_API_KEY) apiKey: String,
    ): Response<MealResponseDto<MealDto>>

    @GET(URL_CATEGORIES)
    suspend fun listMealCategories(
        @Path(PATH_API_KEY) apiKey: String,
    ): Response<CategoryResponseDto>

    @GET(URL_LIST)
    suspend fun listAllCategories(
        @Path(PATH_API_KEY) apiKey: String,
        @Query(QUERY_CATEGORY) query: String,
    ): Response<MealResponseDto<CategoryDto>>

    @GET(URL_LIST)
    suspend fun listAllArea(
        @Path(PATH_API_KEY) apiKey: String,
        @Query(QUERY_AREA) query: String,
    ): Response<MealResponseDto<AreaDto>>

    @GET(URL_LIST)
    suspend fun listAllIngredients(
        @Path(PATH_API_KEY) apiKey: String,
        @Query(QUERY_INGREDIENT) query: String,
    ): Response<MealResponseDto<IngredientDto>>

    @GET(URL_FILTER)
    suspend fun filterByIngredient(
        @Path(PATH_API_KEY) apiKey: String,
        @Query(QUERY_INGREDIENT) ingredient: String,
    ): Response<MealResponseDto<MealFilterDto>>

    @GET(URL_FILTER)
    suspend fun filterByCategory(
        @Path(PATH_API_KEY) apiKey: String,
        @Query(QUERY_CATEGORY) category: String,
    ): Response<MealResponseDto<MealFilterDto>>

    @GET(URL_FILTER)
    suspend fun filterByArea(
        @Path(PATH_API_KEY) apiKey: String,
        @Query(QUERY_AREA) area: String,
    ): Response<MealResponseDto<MealFilterDto>>
}
