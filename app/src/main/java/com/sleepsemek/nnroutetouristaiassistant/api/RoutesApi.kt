package com.sleepsemek.nnroutetouristaiassistant.api

import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteRequest
import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteResponseList
import retrofit2.http.Body
import retrofit2.http.POST

interface RoutesApi {
    @POST("api/routes")
    suspend fun getRoutes(@Body request: RouteRequest): RouteResponseList
}
