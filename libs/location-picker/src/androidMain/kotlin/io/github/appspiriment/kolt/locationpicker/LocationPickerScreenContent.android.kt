package io.github.appspiriment.kolt.locationpicker

// Uses compose-kmp's theme + component library (Kolt.colors/typography, AppsButton,
// AppsValidatedTextField, AppsCircularProgress) — mandatory reuse per kolt-libs.md. This same
// body is duplicated in the iOS and Desktop actuals (not shared via commonMain) because
// compose-kmp has no wasmJs target, and commonMain dependencies must resolve for every enabled
// target including wasmJs — see build.gradle.kts and LocationPickerScreenContent.wasmJs.kt for
// the documented exception this forces. Tabs (PrimaryTabRow) and the timezone dropdown
// (ExposedDropdownMenuBox) stay plain Material3: kolt-libs.md's component table has no tab or
// dropdown-menu entry to reuse.

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.appspiriment.kolt.composekmp.components.core.buttons.AppsButton
import io.github.appspiriment.kolt.composekmp.components.core.buttons.AppsTextButton
import io.github.appspiriment.kolt.composekmp.components.core.buttons.types.ButtonStyle
import io.github.appspiriment.kolt.composekmp.components.core.progress.AppsCircularProgress
import io.github.appspiriment.kolt.composekmp.components.core.text.textfield.AppsValidatedTextField
import io.github.appspiriment.kolt.composekmp.components.core.text.textfield.ValidatedTextFieldState
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.LocalColors
import io.github.appspiriment.kolt.composekmp.theme.getDarkColors
import io.github.appspiriment.kolt.composekmp.theme.getLightColors
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText
import io.github.appspiriment.kolt.location.PlaceSearchResult
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.offsetAt

/**
 * Maps the picker's small [LocationPickerColors] surface onto compose-kmp's full [BaseColors]
 * palette, so that [LocalColors] can be overridden for the fields below AND for compose-kmp's
 * own components (`AppsButton`, `AppsValidatedTextField`, `AppsCircularProgress`) that read
 * [Kolt.colors] internally with no per-call override parameter. Unmapped fields fall back to the
 * light/dark base palette (picked by [background]'s luminance) instead of [Color.Unspecified],
 * so components outside the 8 mapped fields still render coherently.
 */
private fun LocationPickerColors.toBaseColors() =
    (if (background.luminance() < 0.5f) getDarkColors() else getLightColors()).copy(
        background = background,
        onBackground = onSurface,
        mainSurface = surface,
        onMainSurface = onSurface,
        primary = accent,
        onPrimary = onAccent,
        subText = subText,
        error = error,
        dividerColor = border,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal actual fun LocationPickerScreenContent(
    state: LocationPickerState,
    onIntent: (LocationPickerIntent) -> Unit,
    config: LocationPickerConfig,
    modifier: Modifier,
    colors: LocationPickerColors?,
) = CompositionLocalProvider(LocalColors provides (colors?.toBaseColors() ?: LocalColors.current)) {
    Column(modifier.fillMaxSize().background(Kolt.colors.background).padding(16.dp)) {
        Text(config.title, style = MaterialTheme.typography.titleLarge, color = Kolt.colors.onBackground)
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
            AppsTextButton(text = config.cancelLabel.toUiText(), onClick = { onIntent(LocationPickerIntent.Cancel) })
            AppsButton(
                text = config.confirmLabel.toUiText(),
                enabled = state.selectedTab == LocationPickerTab.MANUAL && state.isValid,
                onClick = { onIntent(LocationPickerIntent.Confirm) },
            )
        }
    }
}

private fun LocationPickerTab.contentLabel(config: LocationPickerConfig): String = when (this) {
    LocationPickerTab.SEARCH -> config.searchTabLabel
    LocationPickerTab.MAP -> config.mapTabLabel
    LocationPickerTab.CURRENT -> config.currentLocationTabLabel
    LocationPickerTab.MANUAL -> config.manualTabLabel
}

/**
 * Bridges a hoisted `String` (the single source of truth, per presentation-mvi.md) with
 * `AppsValidatedTextField`'s own internally-remembered [ValidatedTextFieldState] — the component
 * owns its buffer rather than taking value/onValueChange, so external updates (search result
 * selection, current-location fetch, map pick) are pushed in and keystrokes are pushed out.
 */
@Composable
private fun rememberSyncedFieldState(text: String, onTextChange: (String) -> Unit): ValidatedTextFieldState {
    val fieldState = remember { ValidatedTextFieldState(initialValue = text) }
    LaunchedEffect(text) {
        if (fieldState.value != text) fieldState.textFieldState.edit { replace(0, length, text) }
    }
    LaunchedEffect(fieldState) {
        snapshotFlow { fieldState.value }.collect { newValue -> if (newValue != text) onTextChange(newValue) }
    }
    return fieldState
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
    Column(Modifier.fillMaxWidth()) {
        AppsValidatedTextField(
            state = rememberSyncedFieldState(placeQuery, onQueryChange),
            label = config.searchFieldLabel.toUiText(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        when {
            isSearching -> Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppsCircularProgress(Modifier.size(16.dp))
                Text(config.searchingLabel, color = Kolt.colors.subText)
            }

            placeQuery.length >= 3 && placeResults.isEmpty() ->
                Text(config.searchNoResultsLabel, color = Kolt.colors.subText)

            placeResults.isNotEmpty() -> LazyColumn(Modifier.fillMaxWidth().heightIn(max = 260.dp)) {
                items(placeResults) { place ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onResultSelected(place) }
                            .padding(horizontal = 4.dp, vertical = 12.dp)
                    ) {
                        Text(place.label, color = Kolt.colors.onBackground)
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
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Kolt.colors.mainSurface, RoundedCornerShape(14.dp))
                .clickable(enabled = !isFetching, onClick = onTrigger)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (isFetching) {
                AppsCircularProgress(Modifier.size(18.dp))
                Text(config.fetchingLocationLabel, color = Kolt.colors.primary, modifier = Modifier.weight(1f))
            } else {
                Text(config.useCurrentLocationLabel, color = Kolt.colors.primary, modifier = Modifier.weight(1f))
            }
        }
        if (error != null) {
            Text(error, color = Kolt.colors.error, modifier = Modifier.padding(top = 10.dp))
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
    if (isResolvingTimezone) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AppsCircularProgress(Modifier.size(24.dp))
                Spacer(Modifier.height(12.dp))
                Text(config.resolvingTimezoneLabel, color = Kolt.colors.subText)
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
            Text(locationError, color = Kolt.colors.error)
        }
        AppsValidatedTextField(
            state = rememberSyncedFieldState(label, onLabelChange),
            label = config.nameFieldLabel.toUiText(),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppsValidatedTextField(
                state = rememberSyncedFieldState(latitude, onLatitudeChange),
                label = config.latitudeFieldLabel.toUiText(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
            )
            AppsValidatedTextField(
                state = rememberSyncedFieldState(longitude, onLongitudeChange),
                label = config.longitudeFieldLabel.toUiText(),
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
            Text(config.invalidLatitudeLabel, color = Kolt.colors.error)
        }
        if (longitude.isNotBlank() && longitude.toDoubleOrNull() == null) {
            Text(config.invalidLongitudeLabel, color = Kolt.colors.error)
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

// Android-only @Preview — presentation-mvi.md asks for CMP's unified commonMain Preview, which
// needs Compose Multiplatform 1.10+ (this repo pins 1.8.0, see build.gradle.kts's kmp {} block
// history / the retrofit plan). This is the closest available equivalent at the pinned version.
@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun LocationPickerScreenContentPreview() {
    LocationPickerScreenContent(
        state = LocationPickerState(
            selectedTab = LocationPickerTab.MANUAL,
            label = "Bengaluru",
            latitude = "12.9716",
            longitude = "77.5946",
            timezoneId = "Asia/Kolkata",
        ),
        onIntent = {},
        config = LocationPickerConfig(),
        modifier = Modifier,
        colors = null,
    )
}
