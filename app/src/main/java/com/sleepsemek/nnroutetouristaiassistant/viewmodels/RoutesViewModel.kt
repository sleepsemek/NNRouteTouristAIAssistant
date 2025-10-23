package com.sleepsemek.nnroutetouristaiassistant.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteResponse
import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteResponseList
import com.sleepsemek.nnroutetouristaiassistant.data.models.coordinate
import com.sleepsemek.nnroutetouristaiassistant.data.ui.BottomSheetMode
import com.sleepsemek.nnroutetouristaiassistant.data.ui.FocusCoordinate
import com.sleepsemek.nnroutetouristaiassistant.data.ui.SelectedPoint
import com.sleepsemek.nnroutetouristaiassistant.data.ui.UiState
import com.sleepsemek.nnroutetouristaiassistant.di.RoutesRepository
import com.sleepsemek.nnroutetouristaiassistant.ui.components.InterestCategory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Geo
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.location.LocationManagerUtils
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.FitnessOptions
import com.yandex.mapkit.transport.masstransit.PedestrianRouter
import com.yandex.mapkit.transport.masstransit.Route
import com.yandex.mapkit.transport.masstransit.RouteOptions
import com.yandex.mapkit.transport.masstransit.Session
import com.yandex.mapkit.transport.masstransit.Session.RouteListener
import com.yandex.mapkit.transport.masstransit.TimeOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
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

                val location: Point? = if (currentState.useLocation) {
                    LocationManagerUtils.getLastKnownLocation()?.position.takeIf { it != null } ?: run { //TODO: Под вопросом проверка
                        showError("Не удалось получить текущее местоположение")
                        return@launch
                    }
                } else null

                val response = repository.fetchRoutes(interests, currentState.walkingTime, location)
                if (response.routes.isEmpty()) {
                    showError("Не удалось получить маршруты")
                    return@launch
                }

                val requestPoints = mutableListOf<RequestPoint>()
                location?.let {
                    requestPoints.add(RequestPoint(Point(it.latitude, it.longitude), RequestPointType.WAYPOINT, null, null, null))
                }
                response.routes.forEach { route ->
                    val point = Point(route.coordinate.latitude, route.coordinate.longitude)
                    requestPoints.add(RequestPoint(point, RequestPointType.WAYPOINT, null, null, null))
                }

                val timeOptions = TimeOptions()
                val fitnessOptions = FitnessOptions(false, false)
                val routeOptions = RouteOptions(fitnessOptions)

                _uiState.update { it.copy(isLoading = true) }

                val listener = object : RouteListener {
                    override fun onMasstransitRoutes(routes: List<Route?>) {
                        if (routes.isEmpty()) {
                            _uiState.update {
                                it.copy(
                                    error = "Не удалось построить маршрут, проверьте ваше местоположение",
                                    isLoading = false,
                                )
                            }
                        }
                        val builtRoute = routes.firstOrNull() ?: return

                        _uiState.update {
                            it.copy(
                                routes = it.routes.copy(
                                    routes = response.routes.mapIndexed { index, route ->
                                        val sectionIndex = builtRoute.sections.size - response.routes.size + index
                                        val section = builtRoute.sections.getOrNull(sectionIndex)
                                        if (section != null) {
                                            route.copy(
                                                time = section.metadata.weight.time.text,
                                                distance = section.metadata.weight.walkingDistance.text
                                            )
                                        } else {
                                            route
                                        }
                                    },
                                    explanation = response.explanation
                                ),
                                routePolyline = builtRoute.geometry,
                                isLoading = false,
                                mode = BottomSheetMode.Timeline(routeId = response.routes.firstOrNull()?.address?.toString() ?: ""),
                                focusCoordinate = response.routes.firstOrNull()?.coordinate?.let { FocusCoordinate(it) },
                                error = null
                            )
                        }

                        viewModelScope.launch {
                            while (isActive) {
                                if (_uiState.value.routePolyline == null) break

                                _uiState.update {
                                    it.copy(
                                        routePolyline = updatePolylineWithUserLocation(
                                            LocationManagerUtils.getLastKnownLocation()?.position,
                                            builtRoute.geometry) ?: it.routePolyline
                                    )
                                }
                                delay(5000)
                            }
                        }
                    }

                    override fun onMasstransitRoutesError(error: com.yandex.runtime.Error) {
                        _uiState.update {
                            it.copy(
                                error = "Произошла ошибка построения маршрута",
                                isLoading = false,
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
            delay(timeoutMs)
            _uiState.update { it.copy(error = null) }
        }
    }

    fun clearRoutes() {
        routerSession?.cancel()
        routerSession = null

        _uiState.update {
            it.copy(
                isLoading = false,
                focusCoordinate = null,
                routePolyline = null,
                selectedPointIndex = null,
                routes = RouteResponseList(emptyList(), ""),
                mode = BottomSheetMode.Planner
            )
        }
    }

    fun focusOnRoute(route: RouteResponse) {
        _uiState.update { it.copy(focusCoordinate = FocusCoordinate(route.coordinate)) }
    }

    fun setRouterSession(session: Session?) {
        routerSession?.cancel()
        routerSession = session
    }

    fun selectPointAt(index: Int) {
        _uiState.update { it.copy(selectedPointIndex = SelectedPoint(index)) }
    }

    fun updatePolylineWithUserLocation(userLocation: Point?, route: Polyline?): Polyline? {
        if (userLocation == null || route == null) return null

        var minDistance = Double.MAX_VALUE
        var closestIndex = 0

        route.points.forEachIndexed { index, point ->
            val distance = Geo.distance(userLocation, point)
            if (distance < minDistance) {
                minDistance = distance
                closestIndex = index
            }
        }

        if (closestIndex >= route.points.size - 1) return route
        val newPoints = route.points.drop(closestIndex)
        return Polyline(newPoints)
    }

}