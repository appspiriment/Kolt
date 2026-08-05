@file:OptIn(ExperimentalTime::class)

package io.github.appspiriment.kolt.locationpicker

// Plain Material3 — the documented exception to kolt-libs.md's mandatory compose-kmp reuse:
// compose-kmp has no wasmJs target, so this module's Web target can't depend on it (a commonMain
// dependency must resolve for every enabled target, including wasmJs). See build.gradle.kts and
// the android/ios/desktop LocationPickerScreenContent actuals (which do use compose-kmp) for the
// counterpart this duplicates.

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.appspiriment.kolt.location.PlaceSearchResult
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Maps the picker's small [LocationPickerColors] surface onto Material3's full [ColorScheme] —
 * the Web counterpart of the android/ios/desktop actuals' `toBaseColors()`, since this target
 * has no compose-kmp dependency (see the file header) and reads `MaterialTheme.colorScheme`
 * directly instead of `Kolt.colors`. Unmapped fields keep the ambient scheme's values instead of
 * being zeroed out, so components outside the 8 mapped fields still render coherently.
 */
@Composable
private fun LocationPickerColors.toColorScheme(ambient: androidx.compose.material3.ColorScheme) = ambient.copy(
    background = background,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    primary = accent,
    onPrimary = onAccent,
    onSurfaceVariant = subText,
    error = error,
    outline = border,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun LocationPickerScreenContent(
    state: LocationPickerState,
    onIntent: (LocationPickerIntent) -> Unit,
    config: LocationPickerConfig,
    modifier: Modifier,
    colors: LocationPickerColors?,
) {
    val colorScheme = colors?.toColorScheme(MaterialTheme.colorScheme) ?: MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().background(colorScheme.background).padding(16.dp)) {
        Text(config.title, style = MaterialTheme.typography.titleLarge, color = colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))

        val tabs = config.enabledTabs
        PrimaryTabRow(selectedTabIndex = tabs.indexOf(state.selectedTab).coerceAtLeast(0)) {
            tabs.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { onIntent(LocationPickerIntent.SelectTab(tab)) },
                    text = { Text(tab.contentLabel(config), fontSize = 12.sp) },
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (state.selectedTab) {
                LocationPickerTab.SEARCH -> SearchTabContent(
                    config = config,
                    placeQuery = state.placeQuery,
                    onQueryChange = { onIntent(LocationPickerIntent.SearchQueryChanged(it)) },
                    placeResults = state.placeResults,
                    isSearching = state.isSearchingPlaces,
                    onResultSelected = { onIntent(LocationPickerIntent.SearchResultSelected(it)) },
                )

                LocationPickerTab.MAP -> MapPickerContent(
                    modifier = Modifier.fillMaxWidth(),
                    initialLatitude = state.latitude.toDoubleOrNull() ?: config.initialLatitude,
                    initialLongitude = state.longitude.toDoubleOrNull() ?: config.initialLongitude,
                    onPicked = { onIntent(LocationPickerIntent.MapPicked(it)) },
                )

                LocationPickerTab.CURRENT -> CurrentLocationTabContent(
                    config = config,
                    isFetching = state.isFetchingLocation,
                    error = state.locationError,
                    onTrigger = { onIntent(LocationPickerIntent.TriggerCurrentLocation) },
                )

                LocationPickerTab.MANUAL -> ManualEntryForm(
                    config = config,
                    isResolvingTimezone = state.isResolvingTimezone,
                    locationError = state.locationError,
                    label = state.label,
                    onLabelChange = { onIntent(LocationPickerIntent.ChangeLabel(it)) },
                    latitude = state.latitude,
                    onLatitudeChange = { onIntent(LocationPickerIntent.ChangeLatitude(it)) },
                    longitude = state.longitude,
                    onLongitudeChange = { onIntent(LocationPickerIntent.ChangeLongitude(it)) },
                    timezoneId = state.timezoneId,
                    onTimezoneChange = { onIntent(LocationPickerIntent.ChangeTimezone(it)) },
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
            TextButton(onClick = { onIntent(LocationPickerIntent.Cancel) }) {
                Text(config.cancelLabel)
            }
            Button(
                onClick = { onIntent(LocationPickerIntent.Confirm) },
                enabled = state.selectedTab == LocationPickerTab.MANUAL && state.isValid,
            ) {
                Text(config.confirmLabel)
            }
        }
    }
}

private fun LocationPickerTab.contentLabel(config: LocationPickerConfig): String = when (this) {
    LocationPickerTab.SEARCH -> config.searchTabLabel
    LocationPickerTab.MAP -> config.mapTabLabel
    LocationPickerTab.CURRENT -> config.currentLocationTabLabel
    LocationPickerTab.MANUAL -> config.manualTabLabel
}

@Composable
private fun SearchTabContent(
    config: LocationPickerConfig,
    placeQuery: String,
    onQueryChange: (String) -> Unit,
    placeResults: List<PlaceSearchResult>,
    isSearching: Boolean,
    onResultSelected: (PlaceSearchResult) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = placeQuery,
            onValueChange = onQueryChange,
            label = { Text(config.searchFieldLabel) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        when {
            isSearching -> Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                Text(config.searchingLabel, color = colorScheme.onSurfaceVariant)
            }

            placeQuery.length >= 3 && placeResults.isEmpty() ->
                Text(config.searchNoResultsLabel, color = colorScheme.onSurfaceVariant)

            placeResults.isNotEmpty() -> LazyColumn(Modifier.fillMaxWidth().heightIn(max = 260.dp)) {
                items(placeResults) { place ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onResultSelected(place) }
                            .padding(horizontal = 4.dp, vertical = 12.dp)
                    ) {
                        Text(place.label, color = colorScheme.onBackground)
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentLocationTabContent(
    config: LocationPickerConfig,
    isFetching: Boolean,
    error: String?,
    onTrigger: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
                .clickable(enabled = !isFetching, onClick = onTrigger)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (isFetching) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Text(config.fetchingLocationLabel, color = colorScheme.primary, modifier = Modifier.weight(1f))
            } else {
                Text(config.useCurrentLocationLabel, color = colorScheme.primary, modifier = Modifier.weight(1f))
            }
        }
        if (error != null) {
            Text(error, color = colorScheme.error, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualEntryForm(
    config: LocationPickerConfig,
    isResolvingTimezone: Boolean,
    locationError: String?,
    label: String,
    onLabelChange: (String) -> Unit,
    latitude: String,
    onLatitudeChange: (String) -> Unit,
    longitude: String,
    onLongitudeChange: (String) -> Unit,
    timezoneId: String,
    onTimezoneChange: (String) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    if (isResolvingTimezone) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                Spacer(Modifier.height(12.dp))
                Text(config.resolvingTimezoneLabel, color = colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    var timezoneMenuExpanded by remember { mutableStateOf(false) }
    val allTimezoneIds = remember {
        TimeZone.availableZoneIds
            .filter { it == "UTC" || (it.contains('/') && !it.startsWith("Etc/") && !it.startsWith("SystemV/")) }
            .sorted()
    }

    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (locationError != null) {
            Text(locationError, color = colorScheme.error)
        }
        OutlinedTextField(
            value = label,
            onValueChange = onLabelChange,
            label = { Text(config.nameFieldLabel) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = latitude,
                onValueChange = onLatitudeChange,
                label = { Text(config.latitudeFieldLabel) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = longitude,
                onValueChange = onLongitudeChange,
                label = { Text(config.longitudeFieldLabel) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
        }
        ExposedDropdownMenuBox(
            expanded = timezoneMenuExpanded,
            onExpandedChange = { timezoneMenuExpanded = it },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = timezoneDisplayLabel(timezoneId),
                onValueChange = {},
                readOnly = true,
                label = { Text(config.timezoneFieldLabel) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
            )
            ExposedDropdownMenu(
                expanded = timezoneMenuExpanded,
                onDismissRequest = { timezoneMenuExpanded = false },
                modifier = Modifier.heightIn(max = 280.dp),
            ) {
                allTimezoneIds.forEach { tzId ->
                    DropdownMenuItem(
                        text = { Text(timezoneDisplayLabel(tzId)) },
                        onClick = {
                            onTimezoneChange(tzId)
                            timezoneMenuExpanded = false
                        },
                    )
                }
            }
        }
        if (latitude.isNotBlank() && latitude.toDoubleOrNull() == null) {
            Text(config.invalidLatitudeLabel, color = colorScheme.error)
        }
        if (longitude.isNotBlank() && longitude.toDoubleOrNull() == null) {
            Text(config.invalidLongitudeLabel, color = colorScheme.error)
        }
    }
}

private fun timezoneOffsetLabel(timezoneId: String): String = try {
    val totalMinutes = TimeZone.of(timezoneId).offsetAt(Clock.System.now()).totalSeconds / 60
    val sign = if (totalMinutes < 0) "-" else "+"
    val absMinutes = kotlin.math.abs(totalMinutes)
    "$sign${absMinutes / 60}:${(absMinutes % 60).toString().padStart(2, '0')}"
} catch (e: IllegalArgumentException) {
    ""
}

private fun timezoneDisplayLabel(timezoneId: String): String {
    if (timezoneId.isBlank()) return ""
    val offset = timezoneOffsetLabel(timezoneId)
    return if (offset.isNotEmpty()) "$timezoneId  ($offset)" else timezoneId
}
