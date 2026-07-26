package io.github.appspiriment.kolt.locationpicker

import kotlinx.serialization.Serializable

/** Which picking method a tab represents. */
enum class LocationPickerTab { SEARCH, MAP, CURRENT, MANUAL }

/**
 * Controls which tabs are shown and the labels/strings used in [LocationPickerScreen].
 * `@Serializable` so Android can pass it through an Activity Intent as a JSON extra
 * (see [LocationPickerContract]) without pulling in `kotlin-parcelize`.
 *
 * The plain-Kotlin string literals below are fallback defaults only — a data class default
 * can't call a `@Composable` resource lookup. Every `@Composable` entry point in this module
 * (`LocationPickerScreen`, `LocationPicker.rememberLauncher`/`.showDialog`/`.Embed`) defaults to
 * [defaultLocationPickerConfig] instead, which resolves these same labels from
 * `composeResources/values/strings.xml`. These literals are only actually used by the two
 * non-`@Composable` call sites that can't reach a resource: `LocationPickerActivity`'s
 * JSON-decode fallback and iOS's `LocationPicker.present`.
 */
@Serializable
data class LocationPickerConfig(
    val showSearch: Boolean = true,
    val showMap: Boolean = true,
    val showCurrentLocation: Boolean = true,
    val showManualEntry: Boolean = true,
    /** Map tab default center when nothing has been picked yet. */
    val initialLatitude: Double = 20.5937,
    val initialLongitude: Double = 78.9629,
    val title: String = "Add location",
    val searchTabLabel: String = "Search",
    val mapTabLabel: String = "Map",
    val currentLocationTabLabel: String = "Current",
    val manualTabLabel: String = "Manual",
    val searchFieldLabel: String = "Search place",
    val searchingLabel: String = "Searching…",
    val searchNoResultsLabel: String = "No matches",
    val useCurrentLocationLabel: String = "Use current location",
    val fetchingLocationLabel: String = "Fetching current location…",
    val resolvingTimezoneLabel: String = "Detecting timezone…",
    val mapPickerHint: String = "Tap the map to drop a pin",
    val nameFieldLabel: String = "Name",
    val latitudeFieldLabel: String = "Latitude",
    val longitudeFieldLabel: String = "Longitude",
    val timezoneFieldLabel: String = "Timezone",
    val confirmLabel: String = "Add",
    val cancelLabel: String = "Cancel",
    val backLabel: String = "Back",
    val permissionDeniedLabel: String = "Location permission denied",
    val locationUnavailableLabel: String = "Location unavailable",
    val timezoneDetectFailedLabel: String = "Couldn't detect timezone — please pick one",
    val invalidLatitudeLabel: String = "Latitude must be between -90 and 90",
    val invalidLongitudeLabel: String = "Longitude must be between -180 and 180",
) {
    /** First tab to show, honoring the show* flags — [MANUAL] is always available as a fallback. */
    val defaultTab: LocationPickerTab
        get() = when {
            showSearch -> LocationPickerTab.SEARCH
            showMap -> LocationPickerTab.MAP
            showCurrentLocation -> LocationPickerTab.CURRENT
            else -> LocationPickerTab.MANUAL
        }

    val enabledTabs: List<LocationPickerTab>
        get() = buildList {
            if (showSearch) add(LocationPickerTab.SEARCH)
            if (showMap) add(LocationPickerTab.MAP)
            if (showCurrentLocation) add(LocationPickerTab.CURRENT)
            if (showManualEntry) add(LocationPickerTab.MANUAL)
        }
}

/** The location the user confirmed. */
@Serializable
data class LocationPickerResult(
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val timezoneId: String,
)
