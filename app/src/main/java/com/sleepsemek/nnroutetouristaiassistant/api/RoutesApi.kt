package com.sleepsemek.nnroutetouristaiassistant.api

import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteRequest
import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface RoutesApi {
    @POST("api/routes")
    suspend fun getRoutes(@Body request: RouteRequest): List<RouteResponse>
}
