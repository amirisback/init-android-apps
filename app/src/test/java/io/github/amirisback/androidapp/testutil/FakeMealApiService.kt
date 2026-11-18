package io.github.amirisback.androidapp.testutil

import io.github.amirisback.androidapp.data.remote.api.MealApiService
import io.github.amirisback.androidapp.data.remote.dto.AreaDto
import io.github.amirisback.androidapp.data.remote.dto.CategoryDto
import io.github.amirisback.androidapp.data.remote.dto.CategoryResponseDto
import io.github.amirisback.androidapp.data.remote.dto.IngredientDto
import io.github.amirisback.androidapp.data.remote.dto.MealDto
import io.github.amirisback.androidapp.data.remote.dto.MealFilterDto
import io.github.amirisback.androidapp.data.remote.dto.MealResponseDto
import retrofit2.Response

class FakeMealApiService : MealApiService {

    var searchMealResponse: suspend (String, String) -> Response<MealResponseDto<MealDto>> =
        { _, _ -> Response.success(MealResponseDto(emptyList())) }

    var listAllMealResponse: suspend (String, String) -> Response<MealResponseDto<MealDto>> =
        { _, _ -> Response.success(MealResponseDto(emptyList())) }

    var lookupFullMealResponse: suspend (String, String) -> Response<MealResponseDto<MealDto>> =
        { _, _ -> Response.success(MealResponseDto(emptyList())) }

    var lookupRandomMealResponse: suspend (String) -> Response<MealResponseDto<MealDto>> =
        { _ -> Response.success(MealResponseDto(emptyList())) }

    var listMealCategoriesResponse: suspend (String) -> Response<CategoryResponseDto> =
        { _ -> Response.success(CategoryResponseDto(emptyList())) }

    var filterByCategoryResponse: suspend (String, String) -> Response<MealResponseDto<MealFilterDto>> =
        { _, _ -> Response.success(MealResponseDto(emptyList())) }

    var filterByAreaResponse: suspend (String, String) -> Response<MealResponseDto<MealFilterDto>> =
        { _, _ -> Response.success(MealResponseDto(emptyList())) }

    override suspend fun searchMeal(apiKey: String, nameMeal: String): Response<MealResponseDto<MealDto>> =
        searchMealResponse(apiKey, nameMeal)

    override suspend fun listAllMeal(apiKey: String, firstLetter: String): Response<MealResponseDto<MealDto>> =
        listAllMealResponse(apiKey, firstLetter)

    override suspend fun lookupFullMeal(apiKey: String, idMeal: String): Response<MealResponseDto<MealDto>> =
        lookupFullMealResponse(apiKey, idMeal)

    override suspend fun lookupRandomMeal(apiKey: String): Response<MealResponseDto<MealDto>> =
        lookupRandomMealResponse(apiKey)

    override suspend fun listMealCategories(apiKey: String): Response<CategoryResponseDto> =
        listMealCategoriesResponse(apiKey)

    override suspend fun listAllCategories(apiKey: String, query: String): Response<MealResponseDto<CategoryDto>> =
        Response.success(MealResponseDto(emptyList()))

    override suspend fun listAllArea(apiKey: String, query: String): Response<MealResponseDto<AreaDto>> =
        Response.success(MealResponseDto(emptyList()))

    override suspend fun listAllIngredients(apiKey: String, query: String): Response<MealResponseDto<IngredientDto>> =
        Response.success(MealResponseDto(emptyList()))

    override suspend fun filterByIngredient(apiKey: String, ingredient: String): Response<MealResponseDto<MealFilterDto>> =
        Response.success(MealResponseDto(emptyList()))

    override suspend fun filterByCategory(apiKey: String, category: String): Response<MealResponseDto<MealFilterDto>> =
        filterByCategoryResponse(apiKey, category)

    override suspend fun filterByArea(apiKey: String, area: String): Response<MealResponseDto<MealFilterDto>> =
        filterByAreaResponse(apiKey, area)
}
