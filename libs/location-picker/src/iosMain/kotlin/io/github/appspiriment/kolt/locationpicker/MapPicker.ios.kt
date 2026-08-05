package io.github.appspiriment.kolt.locationpicker

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.useContents
import kotlinx.coroutines.launch
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UITapGestureRecognizer
import platform.darwin.NSObject
import platform.darwin.sel_registerName

// Strong-referenced by `tapHandlerHolder` in MapPickerContent below — UIGestureRecognizer
// only holds its target weakly, so without an external owner this would be GC'd and taps
// would silently stop firing.
@OptIn(ExperimentalForeignApi::class)
private class MapTapHandler(
    private val mapView: MKMapView,
    private val annotation: MKPointAnnotation,
    private val onTap: (Double, Double) -> Unit,
) : NSObject() {
    @Suppress("unused")
    @OptIn(BetaInteropApi::class)
    @ObjCAction
    fun handleTap(recognizer: UITapGestureRecognizer) {
        if (recognizer.state != UIGestureRecognizerStateEnded) return
        val point = recognizer.locationInView(mapView)
        val coordinate = mapView.convertPoint(point, toCoordinateFromView = mapView)
        annotation.setCoordinate(coordinate)
        coordinate.useContents { onTap(latitude, longitude) }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapPickerContent(
    modifier: Modifier,
    initialLatitude: Double,
    initialLongitude: Double,
    onPicked: (PickedMapLocation) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var pinLat by remember { mutableStateOf(initialLatitude) }
    var pinLon by remember { mutableStateOf(initialLongitude) }
    var isResolvingLabel by remember { mutableStateOf(false) }
    val tapHandlerHolder = remember { mutableStateOf<MapTapHandler?>(null) }

    Column(modifier) {
        UIKitView(
            factory = {
                val mapView = MKMapView()
                val startCoordinate = CLLocationCoordinate2DMake(initialLatitude, initialLongitude)
                val annotation = MKPointAnnotation().apply { setCoordinate(startCoordinate) }
                mapView.addAnnotation(annotation)
                mapView.setRegion(
                    MKCoordinateRegionMakeWithDistance(startCoordinate, 2_000_000.0, 2_000_000.0),
                    animated = false,
                )

                val handler = MapTapHandler(mapView, annotation) { lat, lon ->
                    pinLat = lat
                    pinLon = lon
                    scope.launch {
                        isResolvingLabel = true
                        onPicked(resolveMapPick(lat, lon))
                        isResolvingLabel = false
                    }
                }
                tapHandlerHolder.value = handler

                val tapRecognizer = UITapGestureRecognizer(
                    target = handler,
                    action = sel_registerName("handleTap:"),
                )
                mapView.addGestureRecognizer(tapRecognizer)
                mapView
            },
            modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(16.dp)),
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
