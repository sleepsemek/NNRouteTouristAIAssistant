package com.sleepsemek.nnroutetouristaiassistant.data.models

import com.google.gson.annotations.SerializedName
import com.yandex.mapkit.geometry.Point

data class RouteRequest(
    val interests: List<String>,
    @SerializedName("walking_time")
    val walkingTime: Int,
    @SerializedName("user_location")
    val userLocation: Point?
)