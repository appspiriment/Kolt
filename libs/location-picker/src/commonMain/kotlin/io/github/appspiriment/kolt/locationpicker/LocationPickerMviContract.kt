package io.github.appspiriment.kolt.locationpicker

import io.github.appspiriment.kolt.location.PlaceSearchResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.TimeZone

// Named LocationPickerMviContract, not LocationPickerContract, to avoid colliding with the
// unrelated androidMain class of a similar name (LocationPickerContract — an
// ActivityResultContract, a different concept).

internal data class LocationPickerState(
    val selectedTab: LocationPickerTab,
    val label: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val timezoneId: String = TimeZone.currentSystemDefault().id,
    val isFetchingLocation: Boolean = false,
    val isResolvingTimezone: Boolean = false,
    val locationError: String? = null,
    val placeQuery: String = "",
    val placeResults: ImmutableList<PlaceSearchResult> = persistentListOf(),
    val isSearchingPlaces: Boolean = false,
) {
    val isValid: Boolean
        get() = label.isNotBlank() &&
            latitude.toDoubleOrNull()?.let { it in -90.0..90.0 } == true &&
            longitude.toDoubleOrNull()?.let { it in -180.0..180.0 } == true &&
            timezoneId.isNotBlank()
}

internal sealed interface LocationPickerIntent {
    data class SelectTab(val tab: LocationPickerTab) : LocationPickerIntent
    data class ChangeLabel(val value: String) : LocationPickerIntent
    data class ChangeLatitude(val value: String) : LocationPickerIntent
    data class ChangeLongitude(val value: String) : LocationPickerIntent
    data class ChangeTimezone(val value: String) : LocationPickerIntent
    data class SearchQueryChanged(val query: String) : LocationPickerIntent
    data class SearchResultSelected(val result: PlaceSearchResult) : LocationPickerIntent
    data class MapPicked(val picked: PickedMapLocation) : LocationPickerIntent
    data object TriggerCurrentLocation : LocationPickerIntent
    data object Confirm : LocationPickerIntent
    data object Cancel : LocationPickerIntent
}

internal sealed interface LocationPickerEffect {
    data class Confirmed(val result: LocationPickerResult) : LocationPickerEffect
    data object Cancelled : LocationPickerEffect
}
