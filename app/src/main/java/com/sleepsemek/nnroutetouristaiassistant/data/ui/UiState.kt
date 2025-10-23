package com.sleepsemek.nnroutetouristaiassistant.data.ui

import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteResponseList
import com.sleepsemek.nnroutetouristaiassistant.ui.components.InterestCategory
import com.yandex.mapkit.geometry.Polyline

data class UiState(
    val mode: BottomSheetMode = BottomSheetMode.Planner,
    val routes: RouteResponseList = RouteResponseList(emptyList(), ""),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedInterests: Set<InterestCategory> = emptySet(),
    val walkingTime: Int = 180,
    val focusCoordinate: FocusCoordinate? = null,
    val routePolyline: Polyline? = null,
    val useLocation: Boolean = true,
    val selectedPointIndex: SelectedPoint? = null
)

sealed interface BottomSheetMode {
    object Planner : BottomSheetMode
    data class Timeline(val routeId: String) : BottomSheetMode
}
