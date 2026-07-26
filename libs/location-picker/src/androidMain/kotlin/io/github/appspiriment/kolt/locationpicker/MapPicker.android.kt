package io.github.appspiriment.kolt.locationpicker

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

private fun ensureOsmdroidConfigured(context: Context) {
    val config = Configuration.getInstance()
    if (config.userAgentValue.isNullOrBlank()) {
        config.load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        config.userAgentValue = context.packageName
    }
}

@Composable
actual fun MapPickerContent(
    modifier: Modifier,
    initialLatitude: Double,
    initialLongitude: Double,
    onPicked: (PickedMapLocation) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pinLat by remember { mutableStateOf(initialLatitude) }
    var pinLon by remember { mutableStateOf(initialLongitude) }
    var isResolvingLabel by remember { mutableStateOf(false) }
    val currentOnTap = rememberUpdatedState<(Double, Double) -> Unit> { lat, lon ->
        pinLat = lat
        pinLon = lon
        scope.launch {
            isResolvingLabel = true
            onPicked(resolveMapPick(lat, lon))
            isResolvingLabel = false
        }
    }

    Column(modifier) {
        AndroidView(
            modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(16.dp)),
            factory = { ctx ->
                ensureOsmdroidConfigured(ctx)
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(5.0)
                    val startPoint = GeoPoint(initialLatitude, initialLongitude)
                    controller.setCenter(startPoint)

                    val marker = Marker(this).apply {
                        position = startPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    overlays.add(marker)

                    overlays.add(
                        MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                marker.position = p
                                invalidate()
                                currentOnTap.value(p.latitude, p.longitude)
                                return true
                            }

                            override fun longPressHelper(p: GeoPoint): Boolean = false
                        })
                    )
                }
            },
            onRelease = { it.onDetach() }
        )

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("${formatCoord(pinLat)}, ${formatCoord(pinLon)}")
            if (isResolvingLabel) {
                CircularProgressIndicator(Modifier.size(12.dp), strokeWidth = 2.dp)
            }
        }
    }
}

private fun formatCoord(value: Double): String {
    val rounded = kotlin.math.round(value * 100000.0) / 100000.0
    return rounded.toString()
}
