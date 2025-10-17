package com.sleepsemek.nnroutetouristaiassistant.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepsemek.nnroutetouristaiassistant.data.models.RouteResponse
import com.sleepsemek.nnroutetouristaiassistant.data.ui.BottomSheetMode
import com.sleepsemek.nnroutetouristaiassistant.data.ui.UiState
import com.sleepsemek.nnroutetouristaiassistant.di.RoutesRepository
import com.sleepsemek.nnroutetouristaiassistant.ui.components.InterestCategory
import com.yandex.mapkit.transport.masstransit.Session
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val repository: RoutesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var routerSession: Session? = null

    fun updateSelectedInterests(interests: Set<InterestCategory>) {
        _uiState.update { it.copy(selectedInterests = interests) }
    }

    fun updateWalkingTime(walkingTime: Float) {
        _uiState.update { it.copy(walkingTime = walkingTime) }
    }

    fun loadRoutes() {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (currentState.selectedInterests.isEmpty()) {
                showError("Выберите категории интересов для построения маршрута")
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val interests = currentState.selectedInterests.map { it.name }
                val response = repository.fetchRoutes(interests, currentState.walkingTime)

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
                showError(e.message ?: "Ошибка загрузки маршрутов")
            }
        }
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

    fun showPlanner() {
        _uiState.update { it.copy(mode = BottomSheetMode.Planner) }
    }

    fun showTimeline(routeId: String) {
        _uiState.update { it.copy(mode = BottomSheetMode.Timeline(routeId), selectedRouteId = routeId) }
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

    fun resetPlanner() {
        _uiState.update {
            it.copy(
                selectedInterests = emptySet(),
                walkingTime = 2f,
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