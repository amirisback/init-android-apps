package io.github.amirisback.androidapp.domain.model

data class Meal(
    val tableId: Int = 0,
    val id: String = "",
    val name: String = "",
    val drinkAlternate: String? = null,
    val category: String = "",
    val area: String = "",
    val instructions: String = "",
    val thumbnailUrl: String = "",
    val tags: String? = null,
    val youtubeUrl: String? = null,
    val ingredients: List<String> = emptyList(),
    val measures: List<String> = emptyList(),
    val sourceUrl: String? = null,
    val dateModified: String? = null,
)
