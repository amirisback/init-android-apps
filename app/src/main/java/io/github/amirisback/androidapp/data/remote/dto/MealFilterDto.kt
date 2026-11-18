package io.github.amirisback.androidapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MealFilterDto(
    @SerializedName("idMeal")
    val idMeal: String? = null,
    @SerializedName("strMeal")
    val strMeal: String? = null,
    @SerializedName("strMealThumb")
    val strMealThumb: String? = null,
)
