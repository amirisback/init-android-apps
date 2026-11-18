package io.github.amirisback.androidapp.data.repository

import io.github.amirisback.androidapp.core.common.Resource
import io.github.amirisback.androidapp.data.local.MealLocalDataSource
import io.github.amirisback.androidapp.data.mapper.toDomain
import io.github.amirisback.androidapp.data.mapper.toEntity
import io.github.amirisback.androidapp.data.remote.MealRemoteDataSource
import io.github.amirisback.androidapp.domain.model.Area
import io.github.amirisback.androidapp.domain.model.Category
import io.github.amirisback.androidapp.domain.model.Ingredient
import io.github.amirisback.androidapp.domain.model.Meal
import io.github.amirisback.androidapp.domain.model.MealFilter
import io.github.amirisback.androidapp.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MealRepositoryImpl @Inject constructor(
    private val remoteDataSource: MealRemoteDataSource,
    private val localDataSource: MealLocalDataSource,
) : MealRepository {

    override fun searchMeal(nameMeal: String): Flow<Resource<List<Meal>>> =
        remoteDataSource.searchMeal(nameMeal).mapResource { dto ->
            dto?.meals?.map { it.toDomain() }.orEmpty()
        }

    override fun listAllMeal(firstLetter: String): Flow<Resource<List<Meal>>> =
        remoteDataSource.listAllMeal(firstLetter).mapResource { dto ->
            dto?.meals?.map { it.toDomain() }.orEmpty()
        }

    override fun lookupFullMeal(idMeal: String): Flow<Resource<List<Meal>>> =
        remoteDataSource.lookupFullMeal(idMeal).mapResource { dto ->
            dto?.meals?.map { it.toDomain() }.orEmpty()
        }

    override fun lookupRandomMeal(): Flow<Resource<List<Meal>>> =
        remoteDataSource.lookupRandomMeal().mapResource { dto ->
            dto?.meals?.map { it.toDomain() }.orEmpty()
        }

    override fun listMealCategories(): Flow<Resource<List<Category>>> =
        remoteDataSource.listMealCategories().mapResource { dto ->
            dto?.categories?.map { it.toDomain() }.orEmpty()
        }

    override fun listAllCategories(query: String): Flow<Resource<List<Category>>> =
        remoteDataSource.listAllCategories(query).mapResource { dto ->
            dto?.meals?.map { it.toDomain() }.orEmpty()
        }

    override fun listAllArea(query: String): Flow<Resource<List<Area>>> =
        remoteDataSource.listAllArea(query).mapResource { dto ->
            dto?.meals?.map { it.toDomain() }.orEmpty()
        }

    override fun listAllIngredients(query: String): Flow<Resource<List<Ingredient>>> =
        remoteDataSource.listAllIngredients(query).mapResource { dto ->
            dto?.meals?.map { it.toDomain() }.orEmpty()
        }

    override fun filterByIngredient(ingredient: String): Flow<Resource<List<MealFilter>>> =
        remoteDataSource.filterByIngredient(ingredient).mapResource { dto ->
            dto?.meals?.map { it.toDomain() }.orEmpty()
        }

    override fun filterByCategory(category: String): Flow<Resource<List<MealFilter>>> =
        remoteDataSource.filterByCategory(category).mapResource { dto ->
            dto?.meals?.map { it.toDomain() }.orEmpty()
        }

    override fun filterByArea(area: String): Flow<Resource<List<MealFilter>>> =
        remoteDataSource.filterByArea(area).mapResource { dto ->
            dto?.meals?.map { it.toDomain() }.orEmpty()
        }

    override fun insertToFavorite(data: Meal): Flow<Resource<Meal>> =
        localDataSource.insertToFavorite(data.toEntity()).mapResource { entity ->
            entity.toDomain()
        }

    override fun getAllFavorite(): Flow<Resource<List<Meal>>> =
        localDataSource.getAllFavorite().mapResource { entities ->
            entities.map { it.toDomain() }
        }

    override fun searchById(idMeal: String): Flow<Resource<List<Meal>>> =
        localDataSource.searchById(idMeal).mapResource { entities ->
            entities.map { it.toDomain() }
        }

    override fun deleteFromTableId(tableId: Int): Flow<Resource<String>> =
        localDataSource.deleteFromTableId(tableId)

    override fun deleteFromMealId(mealId: String): Flow<Resource<String>> =
        localDataSource.deleteFromMealId(mealId)

    override fun nukeData(): Flow<Resource<String>> =
        localDataSource.nukeData()

    private inline fun <T, R> Flow<Resource<T>>.mapResource(
        crossinline transform: (T) -> R,
    ): Flow<Resource<R>> = map { resource ->
        when (resource) {
            is Resource.Loading -> Resource.Loading
            is Resource.Success -> Resource.Success(transform(resource.data))
            is Resource.Error -> Resource.Error(resource.message, resource.throwable)
        }
    }
}
