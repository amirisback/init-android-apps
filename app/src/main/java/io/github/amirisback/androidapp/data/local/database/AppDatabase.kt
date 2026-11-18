package io.github.amirisback.androidapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.amirisback.androidapp.data.local.dao.MealDao
import io.github.amirisback.androidapp.data.local.entity.MealEntity

@Database(
    entities = [MealEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
}
