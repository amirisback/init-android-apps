package io.github.amirisback.androidapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.github.amirisback.androidapp.data.local.database.DatabaseConfig.TABLE_MEALS
import io.github.amirisback.androidapp.data.local.entity.MealEntity

@Dao
interface MealDao {

    @Query("SELECT * FROM $TABLE_MEALS")
    suspend fun getAllData(): List<MealEntity>

    @Query("SELECT * FROM $TABLE_MEALS WHERE idMeal = :idMeal")
    suspend fun searchById(idMeal: String): List<MealEntity>

    @Insert
    suspend fun insertData(data: MealEntity)

    @Query("DELETE FROM $TABLE_MEALS WHERE table_id = :tableId")
    suspend fun deleteDataFromTableId(tableId: Int)

    @Query("DELETE FROM $TABLE_MEALS WHERE idMeal = :idMeal")
    suspend fun deleteDataFromMealId(idMeal: String)

    @Query("DELETE FROM $TABLE_MEALS")
    suspend fun nukeData()
}
