package com.sleepsemek.nnroutetouristaiassistant
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteResponse
import com.sleepsemek.nnroutetouristaiassistant.data.ui.BottomSheetMode
import com.sleepsemek.nnroutetouristaiassistant.ui.components.RoutePlanningSheet
import com.sleepsemek.nnroutetouristaiassistant.ui.components.ScaffoldWithBottomSheet
import com.sleepsemek.nnroutetouristaiassistant.ui.components.TimelineSheet
import com.sleepsemek.nnroutetouristaiassistant.ui.components.YandexMapContent
import com.sleepsemek.nnroutetouristaiassistant.ui.components.rememberBottomSheetController
import com.sleepsemek.nnroutetouristaiassistant.viewmodels.RoutesViewModel

@Composable
fun RoutePlanningScreen(
    viewModel: RoutesViewModel = hiltViewModel()
) {
    val activity = LocalActivity.current as ComponentActivity
    val sheetController = rememberBottomSheetController()
    val uiState by viewModel.uiState.collectAsState()

    val locationPermissions = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (locationPermissions.any { activity.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
        activity.requestPermissions(locationPermissions, 101)
    }

    ScaffoldWithBottomSheet(
        sheetController = sheetController,
        mapContent = { YandexMapContent(activity = activity, viewModel = viewModel) },
        sheetContent = {
            when (uiState.mode) {
                is BottomSheetMode.Planner -> {
                    RoutePlanningSheet(
                        sheetController = sheetController,
                        viewModel = viewModel,
                        isLoading = uiState.isLoading,
                        error = uiState.error
                    )
                }
                is BottomSheetMode.Timeline -> {
                    BackHandler (enabled = true) {
                        viewModel.clearRoutes()
                        sheetController.collapse()
                    }

                    TimelineSheet(
                        sheetController = sheetController,
                        routes = uiState.routes,
                        onClose = {
                            viewModel.clearRoutes()
                            sheetController.collapse()
                        },
                        onSelectStep = { stepIndex ->
                            val selected: RouteResponse? = uiState.routes.getOrNull(stepIndex)
                            if (selected != null) {
                                viewModel.focusOnRoute(selected)
                            }
                        }
                    )
                }
            }
        }
    )
}
