package com.sleepsemek.nnroutetouristaiassistant.data.ui

import com.sleepsemek.nnroutetouristaiassistant.data.models.Coordinate
import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteResponse
import com.sleepsemek.nnroutetouristaiassistant.ui.components.InterestCategory
import com.yandex.mapkit.geometry.Polyline

data class UiState(
    val mode: BottomSheetMode = BottomSheetMode.Planner,
    val routes: List<RouteResponse> = emptyList(),
    val selectedRouteId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedInterests: Set<InterestCategory> = emptySet(),
    val walkingTime: Float = 2f,
    val focusCoordinate: Coordinate? = null,
    val routePolyline: Polyline? = null,
    val isRouteReady: Boolean = false
)

sealed interface BottomSheetMode {
    object Planner : BottomSheetMode
    data class Timeline(val routeId: String) : BottomSheetMode
}
