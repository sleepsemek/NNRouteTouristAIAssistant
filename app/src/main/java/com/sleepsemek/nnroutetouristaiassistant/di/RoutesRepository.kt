package com.sleepsemek.nnroutetouristaiassistant.di

import com.sleepsemek.nnroutetouristaiassistant.api.RoutesApi
import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteRequest
import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteResponseList
import com.yandex.mapkit.geometry.Point

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutesRepository @Inject constructor(
    private val api: RoutesApi
) {
    suspend fun fetchRoutes(interests: List<String>, walkingTime: Int, userLocation: Point?): RouteResponseList {
        return api.getRoutes(RouteRequest(interests, walkingTime, userLocation))
    }
}
