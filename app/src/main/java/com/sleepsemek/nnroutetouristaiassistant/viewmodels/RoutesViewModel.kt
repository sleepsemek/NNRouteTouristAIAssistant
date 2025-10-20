package com.sleepsemek.nnroutetouristaiassistant.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsemek.nnroutetouristaiassistant.data.models.Coordinate
import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteResponse
import com.sleepsemek.nnroutetouristaiassistant.data.ui.BottomSheetMode
import com.sleepsemek.nnroutetouristaiassistant.data.ui.SelectedPoint
import com.sleepsemek.nnroutetouristaiassistant.data.ui.UiState
import com.sleepsemek.nnroutetouristaiassistant.di.RoutesRepository
import com.sleepsemek.nnroutetouristaiassistant.ui.components.InterestCategory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.LocationManagerUtils
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.FitnessOptions
import com.yandex.mapkit.transport.masstransit.PedestrianRouter
import com.yandex.mapkit.transport.masstransit.RouteOptions
import com.yandex.mapkit.transport.masstransit.Session
import com.yandex.mapkit.transport.masstransit.Session.RouteListener
import com.yandex.mapkit.transport.masstransit.TimeOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val repository: RoutesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val router: PedestrianRouter = TransportFactory.getInstance().createPedestrianRouter()
    private var routerSession: Session? = null

    fun updateSelectedInterests(interests: Set<InterestCategory>) {
        _uiState.update { it.copy(selectedInterests = interests) }
    }

    fun updateWalkingTime(walkingTime: Float) {
        _uiState.update { it.copy(walkingTime = walkingTime) }
    }

    fun updateUseLocation(useLocation: Boolean) {
        _uiState.update { it.copy(useLocation = useLocation) }
    }

    fun loadAndBuildRoute() {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (currentState.selectedInterests.isEmpty()) {
                showError("Выберите категории интересов для построения маршрута")
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val interests = currentState.selectedInterests.map { it.name }

                val location = if (currentState.useLocation) {
                    val lastLocation = LocationManagerUtils.getLastKnownLocation()?.position
                    if (lastLocation != null) {
                        Coordinate(lastLocation.latitude, lastLocation.longitude)
                    } else {
                        showError("Не удалось получить текущее местоположение")
                        return@launch
                    }
                } else null

                val response = repository.fetchRoutes(interests, currentState.walkingTime, location)
                if (response.isEmpty()) {
                    showError("Не удалось получить маршруты")
                    return@launch
                }

                val requestPoints = mutableListOf<RequestPoint>()
                location?.let {
                    requestPoints.add(RequestPoint(Point(it.latitude, it.longitude), RequestPointType.WAYPOINT, null, null, null))
                }
                response.forEach { route ->
                    val point = Point(route.coordinate.latitude, route.coordinate.longitude)
                    requestPoints.add(RequestPoint(point, RequestPointType.WAYPOINT, null, null, null))
                }

                val timeOptions = TimeOptions()
                val fitnessOptions = FitnessOptions(false, false)
                val routeOptions = RouteOptions(fitnessOptions)

                _uiState.update { it.copy(isLoading = true) }

                val listener = object : RouteListener {
                    override fun onMasstransitRoutes(routes: List<com.yandex.mapkit.transport.masstransit.Route?>) {
                        if (routes.isEmpty()) {
                            _uiState.update {
                                it.copy(
                                    error = "Не удалось построить маршрут, проверьте ваше местоположение",
                                    isLoading = false,
                                    isRouteReady = false
                                )
                            }
                        }
                        val builtRoute = routes.firstOrNull() ?: return

                        _uiState.update {
                            it.copy(
                                routes = response.mapIndexed { index, _route ->
                                    val sectionIndex = builtRoute.sections.size - response.size + index
                                    val section = builtRoute.sections.getOrNull(sectionIndex)
                                    if (section != null) {
                                        _route.copy(
                                            time = section.metadata.weight.time.text,
                                            distance = section.metadata.weight.walkingDistance.text
                                        )
                                    } else {
                                        _route
                                    }
                                },
                                routePolyline = builtRoute.geometry,
                                isRouteReady = true,
                                isLoading = false,
                                mode = BottomSheetMode.Timeline(routeId = response.firstOrNull()?.id?.toString() ?: ""),
                                focusCoordinate = response.firstOrNull()?.coordinate,
                                error = null
                            )
                        }

                    }

                    override fun onMasstransitRoutesError(error: com.yandex.runtime.Error) {
                        _uiState.update {
                            it.copy(
                                error = "Произошла ошибка построения маршрута",
                                isLoading = false,
                                isRouteReady = false
                            )
                        }
                    }
                }

                routerSession?.cancel()
                routerSession = router.requestRoutes(requestPoints, timeOptions, routeOptions, listener)

            } catch (e: Exception) {
                e.printStackTrace()
                showError("Произошла ошибка при построении маршрута")
            }
        }
    }

    fun showError(message: String, timeoutMs: Long = 3000L) {
        _uiState.update { it.copy(
            error = message,
            isLoading = false,
            mode = BottomSheetMode.Planner
        ) }

        viewModelScope.launch {
            kotlinx.coroutines.delay(timeoutMs)
            _uiState.update { it.copy(error = null) }
        }
    }

    fun clearRoutes() {
        routerSession?.cancel()
        routerSession = null

        _uiState.update {
            it.copy(
                routes = emptyList(),
                mode = BottomSheetMode.Planner
            )
        }
    }

    fun focusOnRoute(route: RouteResponse) {
        _uiState.update { it.copy(focusCoordinate = route.coordinate) }
    }

    fun setRouterSession(session: Session?) {
        routerSession?.cancel()
        routerSession = session
    }

    fun selectPointAt(index: Int) {
        _uiState.update { it.copy(selectedPointIndex = SelectedPoint(index)) }
    }

}