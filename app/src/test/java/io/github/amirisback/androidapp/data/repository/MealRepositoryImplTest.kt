package io.github.amirisback.androidapp.data.repository

import io.github.amirisback.androidapp.core.common.Resource
import io.github.amirisback.androidapp.data.local.MealLocalDataSource
import io.github.amirisback.androidapp.data.remote.MealRemoteDataSource
import io.github.amirisback.androidapp.data.remote.api.MealApiConfig
import io.github.amirisback.androidapp.data.remote.dto.MealDto
import io.github.amirisback.androidapp.data.remote.dto.MealFilterDto
import io.github.amirisback.androidapp.data.remote.dto.MealResponseDto
import io.github.amirisback.androidapp.domain.model.Meal
import io.github.amirisback.androidapp.testutil.FakeMealApiService
import io.github.amirisback.androidapp.testutil.FakeMealDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MealRepositoryImplTest {

    private lateinit var apiService: FakeMealApiService
    private lateinit var mealDao: FakeMealDao
    private lateinit var repository: MealRepositoryImpl

    @Before
    fun setUp() {
        val dispatcher = UnconfinedTestDispatcher()
        apiService = FakeMealApiService()
        mealDao = FakeMealDao()
        repository = MealRepositoryImpl(
            remoteDataSource = MealRemoteDataSource(apiService, dispatcher),
            localDataSource = MealLocalDataSource(mealDao, dispatcher),
        )
    }

    @Test
    fun `searchMeal emits Loading then mapped Success meals using configured API key`() = runTest {
        var capturedKey: String? = null
        apiService.searchMealResponse = { key, _ ->
            capturedKey = key
            Response.success(
                MealResponseDto(
                    meals = listOf(
                        MealDto(
                            idMeal = "101",
                            strMeal = "Creamy Chicken",
                            strCategory = "Chicken",
                            strArea = "French",
                            strMealThumb = "https://example.com/101.jpg",
                        ),
                    ),
                ),
            )
        }

        val emissions = repository.searchMeal("Cream").toList()

        assertEquals(MealApiConfig.API_KEY, capturedKey)
        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        val success = emissions[1] as Resource.Success
        assertEquals(1, success.data.size)
        assertEquals("101", success.data.first().id)
        assertEquals("Creamy Chicken", success.data.first().name)
    }

    @Test
    fun `searchMeal emits Error on HTTP failure response`() = runTest {
        apiService.searchMealResponse = { _, _ ->
            Response.error(500, "Internal Error".toResponseBody("text/plain".toMediaType()))
        }

        val emissions = repository.searchMeal("Cream").toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        assertTrue(emissions[1] is Resource.Error)
    }

    @Test
    fun `searchMeal emits Error when remote throws IOException`() = runTest {
        apiService.searchMealResponse = { _, _ ->
            throw IllegalStateException("No internet connection")
        }

        val emissions = repository.searchMeal("Cream").toList()

        assertEquals(2, emissions.size)
        assertTrue(emissions[0] is Resource.Loading)
        val error = emissions[1] as Resource.Error
        assertEquals("No internet connection", error.message)
    }

    @Test
    fun `filterByCategory and filterByArea map filter DTOs cleanly`() = runTest {
        apiService.filterByCategoryResponse = { _, _ ->
            Response.success(MealResponseDto(listOf(MealFilterDto("1", "Pasta", "thumb.jpg"))))
        }
        apiService.filterByAreaResponse = { _, _ ->
            Response.success(MealResponseDto(listOf(MealFilterDto("2", "Ramen", "ramen.jpg"))))
        }

        val categoryResult = repository.filterByCategory("Pasta").toList().last() as Resource.Success
        val areaResult = repository.filterByArea("Japanese").toList().last() as Resource.Success

        assertEquals("Pasta", categoryResult.data.first().name)
        assertEquals("Ramen", areaResult.data.first().name)
    }

    @Test
    fun `local favorite insert, getAll, searchById, and deleteFromMealId work end-to-end`() = runTest {
        val meal = Meal(id = "52772", name = "Teriyaki Chicken", category = "Chicken", area = "Japanese")

        val insertResult = repository.insertToFavorite(meal).toList()
        assertTrue(insertResult.first() is Resource.Loading)
        assertEquals("52772", (insertResult.last() as Resource.Success).data.id)

        val favorites = (repository.getAllFavorite().toList().last() as Resource.Success).data
        assertEquals(1, favorites.size)
        assertEquals("Teriyaki Chicken", favorites.first().name)

        val found = (repository.searchById("52772").toList().last() as Resource.Success).data
        assertEquals(1, found.size)

        val deleteResult = repository.deleteFromMealId("52772").toList().last()
        assertTrue(deleteResult is Resource.Success)

        val afterDelete = (repository.getAllFavorite().toList().last() as Resource.Success).data
        assertTrue(afterDelete.isEmpty())
    }

    @Test
    fun `getAllFavorite emits Error when DAO throws`() = runTest {
        mealDao.shouldThrow = RuntimeException("DB corrupted")

        val emissions = repository.getAllFavorite().toList()

        assertTrue(emissions.first() is Resource.Loading)
        val error = emissions.last() as Resource.Error
        assertEquals("DB corrupted", error.message)
    }
}
