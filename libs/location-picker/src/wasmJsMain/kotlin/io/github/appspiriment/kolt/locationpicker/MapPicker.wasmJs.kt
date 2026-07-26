package io.github.appspiriment.kolt.locationpicker

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * No map on Web yet — porting a map renderer here means integrating a JS map library (Leaflet,
 * MapLibre, …), which is its own separately-sized piece of work. [LocationPickerConfig.showMap]
 * should be left `false` for web builds; this still needs to compile as a real actual regardless
 * (the `expect` declaration requires one for every enabled target), so it renders a plain notice
 * instead of a stub that would crash if a caller enables the tab anyway.
 */
@Composable
actual fun MapPickerContent(
    modifier: Modifier,
    initialLatitude: Double,
    initialLongitude: Double,
    onPicked: (PickedMapLocation) -> Unit,
) {
    Box(modifier.height(240.dp).padding(16.dp), contentAlignment = Alignment.Center) {
        Text("Map picker isn't available on web yet — use Search or Manual entry.")
    }
}
