package com.sleepsemek.nnroutetouristaiassistant.data.models

import com.google.gson.annotations.SerializedName

data class RouteRequest(
    val interests: List<String>,
    @SerializedName("walking_time")
    val walkingTime: Float
)