package com.sleepsemek.nnroutetouristaiassistant.ui.components

import android.content.Context
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.sleepsemek.nnroutetouristaiassistant.R
import com.sleepsemek.nnroutetouristaiassistant.utils.bitmapFromVectorDualColor
import com.sleepsemek.nnroutetouristaiassistant.viewmodels.RoutesViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider


@Composable
fun YandexMapContent(activity: ComponentActivity, viewModel: RoutesViewModel) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val uiState by viewModel.uiState.collectAsState()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val colorPrimary = MaterialTheme.colorScheme.primary.toArgb()
    val colorTertiary = MaterialTheme.colorScheme.tertiary.toArgb()
    val colorInversePrimary = MaterialTheme.colorScheme.inversePrimary.toArgb()

    val mapObjectsRef = remember { mutableStateOf<MapObjectCollection?>(null) }

    val imageProvider = ImageProvider.fromBitmap(
        context.bitmapFromVectorDualColor(
            R.drawable.map_pin_background,
            colorPrimary,
            R.drawable.map_pin_foreground,
            colorInversePrimary
        )
    )

    DisposableEffect(activity) {
        val lifecycle = activity.lifecycle
        val mapKit = MapKitFactory.getInstance()
        val observer = object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                mapKit.onStart()
                mapView.onStart()
            }
            override fun onStop(owner: LifecycleOwner) {
                mapView.onStop()
                mapKit.onStop()
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            viewModel.setRouterSession(null)
        }
    }

    AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize()) { mv ->
        mv.map.move(CameraPosition(Point(56.3269, 44.0075), 12.0f, 0f, 0f))
        mv.map.isNightModeEnabled = isSystemInDarkTheme
        mapObjectsRef.value = mv.map.mapObjects.addCollection()
        setupUserLocationLayer(context, mv, colorPrimary, colorInversePrimary)
    }

    LaunchedEffect(mapView, uiState.routePolyline) {
        val mapObjects = mapObjectsRef.value ?: return@LaunchedEffect
        mapObjects.clear()

        if (uiState.routes.isEmpty()) return@LaunchedEffect

        val pinsCollection = mapObjects.addCollection()

        uiState.routes.forEachIndexed { index, route ->
            pinsCollection.addPlacemark().apply {
                geometry = Point(route.coordinate.latitude, route.coordinate.longitude)
                setIcon(
                    imageProvider,
                    IconStyle().apply {
                        anchor = PointF(0.5f, 1.0f)
                    }
                )
            }
        }

        val mapPolyline = uiState.routePolyline?.let { mapObjects.addPolyline(it) }
        mapPolyline?.setStrokeColor(colorTertiary)
        mapPolyline?.outlineColor = (colorInversePrimary)
        mapPolyline?.outlineWidth = 2f

        val first = uiState.routes.first().coordinate
        mapView.map.move(
            CameraPosition(Point(first.latitude, first.longitude), 13.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
    }

    LaunchedEffect(uiState.focusCoordinate) {
        val coordinate = uiState.focusCoordinate ?: return@LaunchedEffect
        mapView.map.move(
            CameraPosition(Point(coordinate.latitude, coordinate.longitude), 15.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )
    }
}

private fun setupUserLocationLayer(context: Context, mapView: MapView, colorPrimary: Int, colorInversePrimary: Int) {
    val mapKit = MapKitFactory.getInstance()
    mapKit.resetLocationManagerToDefault()
    val userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)

    userLocationLayer.setObjectListener(object : UserLocationObjectListener {
        override fun onObjectAdded(view: UserLocationView) {
            view.arrow.setIcon(
                ImageProvider.fromBitmap(
                    context.bitmapFromVectorDualColor(
                        R.drawable.nav_compass_background,
                        colorPrimary,
                        R.drawable.nav_compass_foreground,
                        colorInversePrimary,
                    )
                ),
                IconStyle()
                    .setAnchor(PointF(0.5f, 0.5f))
                    .setScale(1f)
            )

            val pinIcon = view.pin.useCompositeIcon()
            pinIcon.setIcon(
                "icon",
                ImageProvider.fromBitmap(
                    context.bitmapFromVectorDualColor(
                        R.drawable.nav_background,
                        colorPrimary,
                        R.drawable.nav_foreground,
                        colorInversePrimary
                    )
                ),
                IconStyle()
                    .setAnchor(PointF(0.5f, 0.5f))
                    .setRotationType(RotationType.ROTATE)
                    .setZIndex(0f)
                    .setScale(1f)
            )
        }

        override fun onObjectRemoved(view: UserLocationView) {}
        override fun onObjectUpdated(view: UserLocationView, p1: ObjectEvent) {}
    })

    userLocationLayer.isVisible = true
    userLocationLayer.isHeadingModeActive = true

}







