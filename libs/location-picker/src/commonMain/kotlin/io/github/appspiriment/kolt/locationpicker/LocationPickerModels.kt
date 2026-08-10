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
    /** Android only — shown in a bottomsheet before the system permission prompt, when needed. */
    val locationPermissionRationaleTitle: String = "Location permission needed",
    val locationPermissionRationaleMessage: String = "We use your location to prefill this field. Grant location access to continue.",
    val locationPermissionRationaleConfirmLabel: String = "Continue",
    val locationPermissionRationaleDismissLabel: String = "Not now",
    /**
     * Android only — fallback bottomsheet, deep-linking to the device's location settings
     * screen. Shown only when `rememberLocationSettingsResolver`'s Play Services check can't
     * resolve the device's location settings automatically (rare) — the common "services are
     * off" case shows Play Services' own in-app dialog instead, without this sheet.
     */
    val locationServicesDisabledTitle: String = "Turn on location services",
    val locationServicesDisabledMessage: String = "Location services are off. Turn them on to use your current location.",
    val locationServicesDisabledConfirmLabel: String = "Open settings",
    val locationServicesDisabledDismissLabel: String = "Not now",
    /** Android only — shown in a bottomsheet when location permission was permanently denied ("Don't ask again"), deep-linking to the app's settings screen instead of the system permission prompt (which won't reappear). */
    val locationPermissionSettingsTitle: String = "Location permission needed",
    val locationPermissionSettingsMessage: String = "Location access was permanently denied. Enable it from app settings to use your current location.",
    val locationPermissionSettingsConfirmLabel: String = "Open settings",
    val locationPermissionSettingsDismissLabel: String = "Not now",
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
