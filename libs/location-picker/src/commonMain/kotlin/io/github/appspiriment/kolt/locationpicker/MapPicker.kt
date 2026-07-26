package io.github.appspiriment.kolt.locationpicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Result of a single map tap: coordinates plus whatever could be resolved for them. */
data class PickedMapLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String?,
    val timezoneId: String?,
)

/**
 * Renders an interactive map (pan/zoom/tap-to-drop-pin) inline — the content of the Map tab.
 * Each platform uses whatever mapping primitive makes sense for it, all free / no API key:
 * Android = OSMDroid, iOS = native MapKit, Desktop = a Canvas renderer over OSM raster tiles.
 *
 * A single tap drops the pin and immediately calls [onPicked] — no separate confirm step.
 * [onPicked] also receives a reverse-geocoded place name and coordinate-resolved timezone
 * (both null if their lookups fail) so the caller can prefill a label and timezone.
 */
@Composable
expect fun MapPickerContent(
    modifier: Modifier,
    initialLatitude: Double,
    initialLongitude: Double,
    onPicked: (PickedMapLocation) -> Unit,
)
