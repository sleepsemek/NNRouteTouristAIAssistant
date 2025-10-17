package com.sleepsemek.nnroutetouristaiassistant.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import androidx.activity.ComponentActivity
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
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
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.sleepsemek.nnroutetouristaiassistant.R
import com.sleepsemek.nnroutetouristaiassistant.viewmodels.RoutesViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.location.LocationManagerUtils
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.transport.TransportFactory
import com.yandex.mapkit.transport.masstransit.FitnessOptions
import com.yandex.mapkit.transport.masstransit.Session.RouteListener
import com.yandex.mapkit.transport.masstransit.RouteOptions
import com.yandex.mapkit.transport.masstransit.TimeOptions
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider

@Composable
fun YandexMapContent(activity: ComponentActivity, viewModel: RoutesViewModel) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val uiState by viewModel.uiState.collectAsState()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val colorPrimary = MaterialTheme.colorScheme.primary.toArgb()
    val colorOnPrimaryContainer = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()

    val mapObjectsRef = remember { mutableStateOf<MapObjectCollection?>(null) }
    val router = remember { TransportFactory.getInstance().createPedestrianRouter() }

    val imageProvider = ImageProvider.fromBitmap(
        context.bitmapFromVector(
            R.drawable.baseline_location_pin_24,
            colorPrimary,
            widthPx = 128,
            heightPx = 128
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
        setupUserLocationLayer(mv)
    }

    LaunchedEffect(uiState.routes) {
        val mapObjects = mapObjectsRef.value ?: return@LaunchedEffect
        mapObjects.clear()

        if (uiState.routes.isEmpty()) return@LaunchedEffect

        val pinsCollection = mapObjects.addCollection()

        val sessionListener = object : RouteListener {
            override fun onMasstransitRoutes(routes: List<com.yandex.mapkit.transport.masstransit.Route?>) {
                val route = routes.firstOrNull() ?: return
                val polyline = route.geometry
                val mapPolyline = mapObjects.addPolyline(polyline)
                mapPolyline.setStrokeColor(colorOnPrimaryContainer)
            }

            override fun onMasstransitRoutesError(error: Error) {
                viewModel.showError("Ошибка построения маршрута: ${error.javaClass.simpleName}")
            }
        }

        val userLocation: Point? = LocationManagerUtils.getLastKnownLocation()?.position

        val requestPoints = mutableListOf<RequestPoint>()

        userLocation?.let {
            requestPoints.add(RequestPoint(it, RequestPointType.WAYPOINT, null, null, null))
        }

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

            val type = when {
                userLocation == null && index == 0 -> RequestPointType.WAYPOINT
                index == uiState.routes.lastIndex -> RequestPointType.WAYPOINT
                else -> RequestPointType.VIAPOINT
            }

            requestPoints.add(RequestPoint(Point(route.coordinate.latitude, route.coordinate.longitude), type, null, null, null))
        }

        val timeOptions = TimeOptions().apply {

        }

        val fitnessOptions = FitnessOptions(false, false)
        val routeOptions = RouteOptions(fitnessOptions)

        val session = router.requestRoutes(requestPoints, timeOptions, routeOptions, sessionListener)
        viewModel.setRouterSession(session)

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

private fun setupUserLocationLayer(mapView: MapView) {
    val mapKit = MapKitFactory.getInstance()

    mapKit.resetLocationManagerToDefault()

    val userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
    userLocationLayer.isVisible = true
    userLocationLayer.isHeadingModeActive = true

    userLocationLayer.setObjectListener(object : UserLocationObjectListener {
        override fun onObjectAdded(view: UserLocationView) {
        }

        override fun onObjectRemoved(view: UserLocationView) {}
        override fun onObjectUpdated(view: UserLocationView, p1: ObjectEvent) {
            view.arrow.setIcon(
                ImageProvider.fromResource(mapView.context, android.R.drawable.arrow_up_float)
            )

            val pinIcon = view.pin.useCompositeIcon()
            pinIcon.setIcon(
                "icon",
                ImageProvider.fromResource(mapView.context, android.R.drawable.ic_menu_mylocation),
                IconStyle()
                    .setAnchor(PointF(0.5f, 0.5f))
                    .setRotationType(RotationType.ROTATE)
                    .setZIndex(0f)
                    .setScale(1f)
            )
        }
    })
}

fun Context.bitmapFromVector(
    @DrawableRes drawableId: Int,
    @ColorInt tintColor: Int? = null,
    widthPx: Int? = null,
    heightPx: Int? = null
): Bitmap {
    val drawable = ContextCompat.getDrawable(this, drawableId) ?: throw IllegalArgumentException("Drawable not found")

    tintColor?.let { DrawableCompat.setTint(drawable, it) }

    val bitmapWidth = widthPx ?: drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
    val bitmapHeight = heightPx ?: drawable.intrinsicHeight.takeIf { it > 0 } ?: 1

    val bitmap = createBitmap(bitmapWidth, bitmapHeight)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}



