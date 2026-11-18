package io.github.amirisback.androidapp.core.common

import io.github.amirisback.androidapp.domain.model.Meal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class JsonExtTest {

    @Test
    fun `toJson and fromJson round-trip pure domain Meal losslessly`() {
        val meal = Meal(
            tableId = 3,
            id = "52772",
            name = "Teriyaki Chicken Casserole",
            category = "Chicken",
            area = "Japanese",
            instructions = "Preheat oven to 350 degrees.",
            thumbnailUrl = "https://www.themealdb.com/images/media/meals/wvpsxx1468256321.jpg",
            tags = "Meat,Casserole",
            youtubeUrl = "https://www.youtube.com/watch?v=4aZr5hZXP_s",
            ingredients = listOf("soy sauce", "water", "brown sugar"),
            measures = listOf("3/4 cup", "1/2 cup", "1/4 cup"),
            sourceUrl = "https://example.com/recipe",
        )

        val json = meal.toJson()
        val decoded = fromJson<Meal>(json)

        assertEquals(meal, decoded)
    }

    @Test
    fun `fromJson returns null for null, blank, or malformed json`() {
        assertNull(fromJson<Meal>(null))
        assertNull(fromJson<Meal>(""))
        assertNull(fromJson<Meal>("   "))
        assertNull(fromJson<Meal>("{invalid json"))
    }
}
