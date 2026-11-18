package io.github.amirisback.androidapp.data.mapper

import io.github.amirisback.androidapp.data.remote.dto.AreaDto
import io.github.amirisback.androidapp.data.remote.dto.CategoryDto
import io.github.amirisback.androidapp.data.remote.dto.IngredientDto
import io.github.amirisback.androidapp.data.remote.dto.MealDto
import io.github.amirisback.androidapp.data.remote.dto.MealFilterDto
import io.github.amirisback.androidapp.domain.model.Meal
import org.junit.Assert.assertEquals
import org.junit.Test

class MealMapperTest {

    @Test
    fun `MealDto toDomain maps fields and filters blank ingredients and measures`() {
        val dto = MealDto(
            idMeal = "52772",
            strMeal = "Creamy Pasta",
            strDrinkAlternate = "Sparkling Water",
            strCategory = "Pasta",
            strArea = "Italian",
            strInstructions = "Boil pasta and mix sauce.",
            strMealThumb = "https://example.com/pasta.jpg",
            strTags = "Creamy,Quick",
            strYoutube = "https://youtube.com/watch?v=123",
            strIngredient1 = "Chicken",
            strIngredient2 = "  Cream  ",
            strIngredient3 = "",
            strIngredient4 = "   ",
            strMeasure1 = "200g",
            strMeasure2 = "1 cup",
            strMeasure3 = null,
            strSource = "https://example.com/source",
            dateModified = "2026-01-01",
        )

        val domain = dto.toDomain()

        assertEquals("52772", domain.id)
        assertEquals("Creamy Pasta", domain.name)
        assertEquals("Sparkling Water", domain.drinkAlternate)
        assertEquals("Pasta", domain.category)
        assertEquals("Italian", domain.area)
        assertEquals("Boil pasta and mix sauce.", domain.instructions)
        assertEquals("https://example.com/pasta.jpg", domain.thumbnailUrl)
        assertEquals("Creamy,Quick", domain.tags)
        assertEquals("https://youtube.com/watch?v=123", domain.youtubeUrl)
        assertEquals(listOf("Chicken", "Cream"), domain.ingredients)
        assertEquals(listOf("200g", "1 cup"), domain.measures)
        assertEquals("https://example.com/source", domain.sourceUrl)
        assertEquals("2026-01-01", domain.dateModified)
    }

    @Test
    fun `Meal toEntity and MealEntity toDomain round-trip is lossless across all 20 slots`() {
        val twentyIngredients = (1..20).map { "Ingredient $it" }
        val twentyMeasures = (1..20).map { "Measure $it" }
        val original = Meal(
            tableId = 7,
            id = "999",
            name = "Full Feast",
            drinkAlternate = "Tea",
            category = "Main",
            area = "Indonesian",
            instructions = "Cook thoroughly.",
            thumbnailUrl = "https://example.com/feast.jpg",
            tags = "Festive",
            youtubeUrl = "https://youtube.com/feast",
            ingredients = twentyIngredients,
            measures = twentyMeasures,
            sourceUrl = "https://example.com/feast",
            dateModified = "2026-11-18",
        )

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `auxiliary DTO mappers convert Category, Area, Ingredient, and MealFilter cleanly`() {
        val category = CategoryDto("1", "Beef", "thumb.jpg", "Beef dishes").toDomain()
        assertEquals("1", category.id)
        assertEquals("Beef", category.name)
        assertEquals("thumb.jpg", category.thumbnailUrl)
        assertEquals("Beef dishes", category.description)

        val area = AreaDto("Japanese").toDomain()
        assertEquals("Japanese", area.name)

        val ingredient = IngredientDto("10", "Garlic", "Aromatic bulb", "Spice").toDomain()
        assertEquals("10", ingredient.id)
        assertEquals("Garlic", ingredient.name)
        assertEquals("Aromatic bulb", ingredient.description)
        assertEquals("Spice", ingredient.type)

        val filter = MealFilterDto("52874", "Beef Pie", "pie.jpg").toDomain()
        assertEquals("52874", filter.id)
        assertEquals("Beef Pie", filter.name)
        assertEquals("pie.jpg", filter.thumbnailUrl)
    }
}
