package io.github.amirisback.androidapp.testutil

import io.github.amirisback.androidapp.data.local.dao.MealDao
import io.github.amirisback.androidapp.data.local.entity.MealEntity

class FakeMealDao : MealDao {

    private val storage = mutableListOf<MealEntity>()
    private var nextId = 1
    var shouldThrow: Exception? = null

    override suspend fun getAllData(): List<MealEntity> {
        shouldThrow?.let { throw it }
        return storage.toList()
    }

    override suspend fun searchById(idMeal: String): List<MealEntity> {
        shouldThrow?.let { throw it }
        return storage.filter { it.idMeal == idMeal }
    }

    override suspend fun insertData(data: MealEntity) {
        shouldThrow?.let { throw it }
        val stored = if (data.tableId == 0) data.copy(tableId = nextId++) else data
        storage.add(stored)
    }

    override suspend fun deleteDataFromTableId(tableId: Int) {
        shouldThrow?.let { throw it }
        storage.removeAll { it.tableId == tableId }
    }

    override suspend fun deleteDataFromMealId(idMeal: String) {
        shouldThrow?.let { throw it }
        storage.removeAll { it.idMeal == idMeal }
    }

    override suspend fun nukeData() {
        shouldThrow?.let { throw it }
        storage.clear()
    }
}
