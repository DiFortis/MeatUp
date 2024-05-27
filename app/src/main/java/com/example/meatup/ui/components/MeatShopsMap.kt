package com.example.meatup.ui.components

import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun MeatShopsMap(userLocation: Location) {
    var mapView by remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        factory = { context ->
            MapView(context).apply {
                setMultiTouchControls(true)
                mapView = this
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    LaunchedEffect(userLocation) {
        mapView?.let { map ->
            if (map.overlays.isEmpty()) {
                val startPoint = GeoPoint(userLocation.latitude, userLocation.longitude)
                map.controller.setZoom(15.0)
                map.controller.setCenter(startPoint)

                val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(map.context), map)
                locationOverlay.enableMyLocation()
                map.overlays.add(locationOverlay)

                val markers = listOf(
                    Marker(map).apply {
                        position = GeoPoint(userLocation.latitude + 0.01, userLocation.longitude + 0.01)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Meat Shop 1"
                    },
                    Marker(map).apply {
                        position = GeoPoint(userLocation.latitude - 0.01, userLocation.longitude - 0.01)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Meat Shop 2"
                    }
                )

                map.overlays.addAll(markers)
            }
        }
    }
}
