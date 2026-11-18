package io.github.amirisback.androidapp.data.local

import io.github.amirisback.androidapp.core.common.Resource
import io.github.amirisback.androidapp.core.di.IoDispatcher
import io.github.amirisback.androidapp.data.local.dao.MealDao
import io.github.amirisback.androidapp.data.local.entity.MealEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealLocalDataSource @Inject constructor(
    private val mealDao: MealDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    fun insertToFavorite(data: MealEntity): Flow<Resource<MealEntity>> = safeDbCall {
        mealDao.insertData(data)
        data
    }

    fun getAllFavorite(): Flow<Resource<List<MealEntity>>> = safeDbCall {
        mealDao.getAllData()
    }

    fun searchById(idMeal: String): Flow<Resource<List<MealEntity>>> = safeDbCall {
        mealDao.searchById(idMeal)
    }

    fun deleteFromTableId(tableId: Int): Flow<Resource<String>> = safeDbCall {
        mealDao.deleteDataFromTableId(tableId)
        "Deleted favorite item"
    }

    fun deleteFromMealId(mealId: String): Flow<Resource<String>> = safeDbCall {
        mealDao.deleteDataFromMealId(mealId)
        "Deleted favorite item"
    }

    fun nukeData(): Flow<Resource<String>> = safeDbCall {
        mealDao.nukeData()
        "Deleted all favorite items"
    }

    private fun <T> safeDbCall(block: suspend () -> T): Flow<Resource<T>> = flow {
        emit(Resource.Loading)
        try {
            emit(Resource.Success(block()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            emit(Resource.Error(message = e.message ?: "Database error", throwable = e))
        }
    }.flowOn(ioDispatcher)
}
