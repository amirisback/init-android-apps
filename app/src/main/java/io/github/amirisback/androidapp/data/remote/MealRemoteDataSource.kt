package io.github.amirisback.androidapp.data.remote

import io.github.amirisback.androidapp.core.common.Resource
import io.github.amirisback.androidapp.core.di.IoDispatcher
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig
import io.github.amirisback.androidapp.data.remote.api.MealApiService
import io.github.amirisback.androidapp.data.remote.dto.AreaDto
import io.github.amirisback.androidapp.data.remote.dto.CategoryDto
import io.github.amirisback.androidapp.data.remote.dto.CategoryResponseDto
import io.github.amirisback.androidapp.data.remote.dto.IngredientDto
import io.github.amirisback.androidapp.data.remote.dto.MealDto
import io.github.amirisback.androidapp.data.remote.dto.MealFilterDto
import io.github.amirisback.androidapp.data.remote.dto.MealResponseDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRemoteDataSource @Inject constructor(
    private val apiService: MealApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    fun searchMeal(nameMeal: String): Flow<Resource<MealResponseDto<MealDto>?>> = safeApiCall {
        apiService.searchMeal(MealApiConfig.API_KEY, nameMeal)
    }

    fun listAllMeal(firstLetter: String): Flow<Resource<MealResponseDto<MealDto>?>> = safeApiCall {
        apiService.listAllMeal(MealApiConfig.API_KEY, firstLetter)
    }

    fun lookupFullMeal(idMeal: String): Flow<Resource<MealResponseDto<MealDto>?>> = safeApiCall {
        apiService.lookupFullMeal(MealApiConfig.API_KEY, idMeal)
    }

    fun lookupRandomMeal(): Flow<Resource<MealResponseDto<MealDto>?>> = safeApiCall {
        apiService.lookupRandomMeal(MealApiConfig.API_KEY)
    }

    fun listMealCategories(): Flow<Resource<CategoryResponseDto?>> = safeApiCall {
        apiService.listMealCategories(MealApiConfig.API_KEY)
    }

    fun listAllCategories(query: String): Flow<Resource<MealResponseDto<CategoryDto>?>> = safeApiCall {
        apiService.listAllCategories(MealApiConfig.API_KEY, query)
    }

    fun listAllArea(query: String): Flow<Resource<MealResponseDto<AreaDto>?>> = safeApiCall {
        apiService.listAllArea(MealApiConfig.API_KEY, query)
    }

    fun listAllIngredients(query: String): Flow<Resource<MealResponseDto<IngredientDto>?>> = safeApiCall {
        apiService.listAllIngredients(MealApiConfig.API_KEY, query)
    }

    fun filterByIngredient(ingredient: String): Flow<Resource<MealResponseDto<MealFilterDto>?>> = safeApiCall {
        apiService.filterByIngredient(MealApiConfig.API_KEY, ingredient)
    }

    fun filterByCategory(category: String): Flow<Resource<MealResponseDto<MealFilterDto>?>> = safeApiCall {
        apiService.filterByCategory(MealApiConfig.API_KEY, category)
    }

    fun filterByArea(area: String): Flow<Resource<MealResponseDto<MealFilterDto>?>> = safeApiCall {
        apiService.filterByArea(MealApiConfig.API_KEY, area)
    }

    private fun <T> safeApiCall(call: suspend () -> Response<T>): Flow<Resource<T?>> = flow {
        emit(Resource.Loading)
        try {
            val response = call()
            if (response.isSuccessful) {
                emit(Resource.Success(response.body()))
            } else {
                emit(Resource.Error(message = response.message().ifBlank { "HTTP ${response.code()}" }))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Network error", throwable = e))
        }
    }.flowOn(ioDispatcher)
}
