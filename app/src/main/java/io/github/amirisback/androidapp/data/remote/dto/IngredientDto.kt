package io.github.amirisback.androidapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class IngredientDto(
    @SerializedName("idIngredient")
    val idIngredient: String? = null,
    @SerializedName("strIngredient")
    val strIngredient: String? = null,
    @SerializedName("strDescription")
    val strDescription: String? = null,
    @SerializedName("strType")
    val strType: String? = null,
)
