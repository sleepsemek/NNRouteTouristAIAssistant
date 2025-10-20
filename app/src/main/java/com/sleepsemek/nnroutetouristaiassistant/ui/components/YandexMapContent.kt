package com.sleepsemek.nnroutetouristaiassistant.ui.components

import android.graphics.PointF
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.sleepsemek.nnroutetouristaiassistant.R
import com.sleepsemek.nnroutetouristaiassistant.utils.bitmapFromVectorDualColor
import com.sleepsemek.nnroutetouristaiassistant.viewmodels.RoutesViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.*
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider

@Composable
fun YandexMapContent(activity: ComponentActivity, viewModel: RoutesViewModel) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val uiState by viewModel.uiState.collectAsState()

    val colorPrimary = MaterialTheme.colorScheme.primary.toArgb()
    val colorTertiary = MaterialTheme.colorScheme.tertiary.toArgb()
    val colorInversePrimary = MaterialTheme.colorScheme.inversePrimary.toArgb()

    val mapPinImage = ImageProvider.fromBitmap(
        context.bitmapFromVectorDualColor(
            R.drawable.map_pin_background,
            colorPrimary,
            R.drawable.map_pin_foreground,
            colorInversePrimary
        )
    )
    val userArrowImage = ImageProvider.fromBitmap(
        context.bitmapFromVectorDualColor(
            R.drawable.nav_compass_background,
            colorPrimary,
            R.drawable.nav_compass_foreground,
            colorInversePrimary
        )
    )
    val userPinImage = ImageProvider.fromBitmap(
        context.bitmapFromVectorDualColor(
            R.drawable.nav_background,
            colorPrimary,
            R.drawable.nav_foreground,
            colorInversePrimary
        )
    )

    val mapView = remember { MapView(context) }
    val mapKit = remember { MapKitFactory.getInstance() }

    val userLocationLayerRef = remember { mapKit.createUserLocationLayer(mapView.mapWindow) }
    val userLocationListener = remember {
        object : UserLocationObjectListener {
            override fun onObjectAdded(view: UserLocationView) {
                applyUserIcons(view, colorInversePrimary, userArrowImage, userPinImage)
            }

            override fun onObjectRemoved(view: UserLocationView) {}
            override fun onObjectUpdated(view: UserLocationView, event: ObjectEvent) {}
        }
    }

    val mapObjectsCollectionRef = remember { mutableStateOf<MapObjectCollection?>(null) }
    val markerList = remember { mutableStateListOf<PlacemarkMapObject>() }
    val markerTapListeners = remember { mutableStateListOf<MapObjectTapListener>() }
    val polylineRef = remember { mutableStateOf<PolylineMapObject?>(null) }

    fun addMarkers() {
        val collection = mapObjectsCollectionRef.value ?: return
        collection.clear()
        markerList.clear()
        markerTapListeners.clear()

        if (uiState.routes.isNotEmpty()) {
            uiState.routes.forEach { route ->
                val marker = collection.addPlacemark(
                    Point(route.coordinate.latitude, route.coordinate.longitude),
                    mapPinImage,
                    IconStyle().setAnchor(PointF(0.5f, 1f))
                )
                marker.userData = route

                val listener = MapObjectTapListener { _, _ ->
                    val index = uiState.routes.indexOf(route)
                    if (index != -1) {
                        viewModel.selectPointAt(index)
                        true
                    } else false
                }

                marker.addTapListener(listener)

                markerList.add(marker)
                markerTapListeners.add(listener)
            }
        }
    }

    fun addPolyline() {
        val collection = mapObjectsCollectionRef.value ?: return
        val newGeometry = uiState.routePolyline ?: return

        val existingPoly = polylineRef.value

        if (existingPoly != null && existingPoly.isValid) {
            existingPoly.geometry = newGeometry
        } else {
            val newPoly = collection.addPolyline(newGeometry)
            newPoly.setStrokeColor(colorTertiary)
            newPoly.outlineColor = colorInversePrimary
            newPoly.outlineWidth = 2f
            polylineRef.value = newPoly
        }
    }

    fun clearMapObjects() {
        mapObjectsCollectionRef.value?.clear()
        markerList.clear()
        markerTapListeners.clear()
        polylineRef.value = null
    }

    DisposableEffect(activity) {
        val lifecycle = activity.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    mapKit.onStart()
                    mapView.onStart()

                    addMarkers()
                    addPolyline()
                }

                Lifecycle.Event.ON_STOP -> {
                    clearMapObjects()
                    mapView.onStop()
                    mapKit.onStop()
                }

                else -> {}
            }
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            clearMapObjects()
            viewModel.setRouterSession(null)
        }
    }

    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { mv ->
        mv.mapWindow.map.move(CameraPosition(Point(56.3269, 44.0075), 12.0f, 0f, 0f))
        mv.mapWindow.map.isNightModeEnabled = isDark
        mapObjectsCollectionRef.value = mv.mapWindow.map.mapObjects.addCollection()
    }

    LaunchedEffect(userLocationLayerRef) {
        userLocationLayerRef.apply {
            isVisible = true
            isHeadingModeActive = true
            setObjectListener(userLocationListener)
        }
    }

    LaunchedEffect(uiState.routes) {
        if (mapObjectsCollectionRef.value != null &&
            activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        ) {
            addMarkers()
        }
    }

    LaunchedEffect(uiState.routePolyline) {
        if (mapObjectsCollectionRef.value != null &&
            activity.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
        ) {
            addPolyline()
        }
    }

    LaunchedEffect(uiState.focusCoordinate) {
        uiState.focusCoordinate?.let { focusCoordinate ->
            mapView.mapWindow.map.move(
                CameraPosition(Point(focusCoordinate.coordinate.latitude, focusCoordinate.coordinate.longitude), 15.0f, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        }
    }
}

private fun applyUserIcons(
    view: UserLocationView,
    accuracyCircleColor: Int,
    mapArrowImage: ImageProvider,
    mapPinImage: ImageProvider
) {
    view.arrow.setIcon(
        mapArrowImage,
        IconStyle().setAnchor(PointF(0.5f, 0.5f)).setScale(1f)
    )

    val pinIcon = view.pin.useCompositeIcon()
    pinIcon.setIcon(
        "icon",
        mapPinImage,
        IconStyle()
            .setAnchor(PointF(0.5f, 0.5f))
            .setRotationType(RotationType.ROTATE)
            .setZIndex(0f)
            .setScale(1f)
    )

    view.accuracyCircle.fillColor = accuracyCircleColor and 0x99FFFFFF.toInt()
}
