package io.github.amirisback.androidapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("idCategory")
    val idCategory: String? = null,
    @SerializedName("strCategory")
    val strCategory: String? = null,
    @SerializedName("strCategoryThumb")
    val strCategoryThumb: String? = null,
    @SerializedName("strCategoryDescription")
    val strCategoryDescription: String? = null,
)
