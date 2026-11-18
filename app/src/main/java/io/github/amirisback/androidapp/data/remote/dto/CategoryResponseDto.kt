package io.github.amirisback.androidapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CategoryResponseDto(
    @SerializedName("categories")
    val categories: List<CategoryDto>? = null,
)
