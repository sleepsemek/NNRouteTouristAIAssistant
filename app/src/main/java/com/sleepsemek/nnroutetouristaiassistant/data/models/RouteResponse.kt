package com.sleepsemek.nnroutetouristaiassistant.data.models

data class RouteResponseList(
    val routes: List<RouteResponse>,
    val explanation: String
)

data class RouteResponse(
    val title: String,
    val description: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val time: String?,
    val distance: String?
)

val RouteResponse.coordinate: Coordinate
    get() = Coordinate(latitude, longitude)

data class Coordinate(
    val latitude: Double,
    val longitude: Double
)