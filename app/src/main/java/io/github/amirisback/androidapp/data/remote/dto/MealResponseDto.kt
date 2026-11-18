package io.github.amirisback.androidapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class MealResponseDto<T>(
    @SerializedName("meals")
    val meals: List<T>? = null,
)
