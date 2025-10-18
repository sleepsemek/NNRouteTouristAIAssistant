package com.sleepsemek.nnroutetouristaiassistant.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsemek.nnroutetouristaiassistant.data.models.Coordinate
import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteResponse
import com.sleepsemek.nnroutetouristaiassistant.data.ui.BottomSheetMode
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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

    init {
        viewModelScope.launch {
            uiState
                .map { it.routes }
                .distinctUntilChanged()
                .collectLatest { routes ->
                    if (routes.isNotEmpty()) {
                        buildRoute(LocationManagerUtils.getLastKnownLocation()?.position)
                    } else {
                        cancelRoute()
                    }
                }
        }
    }

    fun updateSelectedInterests(interests: Set<InterestCategory>) {
        _uiState.update { it.copy(selectedInterests = interests) }
    }

    fun updateWalkingTime(walkingTime: Float) {
        _uiState.update { it.copy(walkingTime = walkingTime) }
    }

    fun updateUseLocation(useLocation: Boolean) {
        _uiState.update { it.copy(useLocation = useLocation) }
    }

    fun loadPointsOfInterest() {
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
                        Coordinate(lastLocation.longitude, lastLocation.latitude)
                    } else {
                        showError("Не удалось получить текущее местоположение")
                        return@launch
                    }
                } else {
                    null
                }

                val response = repository.fetchRoutes(interests, currentState.walkingTime, location)

                _uiState.update {
                    it.copy(
                        routes = response,
                        isLoading = false,
                        mode = BottomSheetMode.Timeline(routeId = response.firstOrNull()?.id?.toString() ?: ""),
                        selectedRouteId = response.firstOrNull()?.id?.toString()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showError("Произошла ошибка загрузки маршрутов")
            }
        }
    }

    fun buildRoute(userLocation: Point?) {
        val routes = _uiState.value.routes
        if (routes.isEmpty()) return

        val requestPoints = mutableListOf<RequestPoint>()

        userLocation?.let {
            requestPoints.add(RequestPoint(it, RequestPointType.WAYPOINT, null, null, null))
        }

        routes.forEachIndexed { index, route ->
            val point = Point(route.coordinate.latitude, route.coordinate.longitude)

            val type = when {
                userLocation == null && index == 0 -> RequestPointType.WAYPOINT
                index == routes.lastIndex -> RequestPointType.WAYPOINT
                else -> RequestPointType.VIAPOINT
            }

            requestPoints.add(RequestPoint(point, type, null, null, null))
        }

        val timeOptions = TimeOptions()
        val fitnessOptions = FitnessOptions(false, false)
        val routeOptions = RouteOptions(fitnessOptions)

        _uiState.update { it.copy(isLoading = true, error = null) }

        val listener = object : RouteListener {
            override fun onMasstransitRoutes(routes: List<com.yandex.mapkit.transport.masstransit.Route?>) {
                val route = routes.firstOrNull() ?: return
                _uiState.update {
                    it.copy(
                        routePolyline = route.geometry,
                        isRouteReady = true,
                        isLoading = false
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
    }

    fun cancelRoute() {
        routerSession?.cancel()
        routerSession = null
        _uiState.update { it.copy(routePolyline = null, isRouteReady = false) }
    }

    fun showError(message: String, timeoutMs: Long = 3000L) {
        _uiState.update { it.copy(
            error = message,
            isLoading = false
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
                selectedRouteId = null,
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

}