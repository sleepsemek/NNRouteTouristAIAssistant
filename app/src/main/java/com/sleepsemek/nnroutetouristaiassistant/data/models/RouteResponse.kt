package com.sleepsemek.nnroutetouristaiassistant.data.models

import com.google.gson.annotations.SerializedName

data class RouteResponse(
    val id: Int,
    val title: String,
    val description: String,
    val address: String,
    val coordinate: Coordinate,
    @SerializedName("category_id")
    val categoryId: String,
    val url: String,
    val time: String?,
    val distance: String?
)