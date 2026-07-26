package io.github.appspiriment.kolt.locationpicker

import io.github.appspiriment.kolt.location.LocationResult
import io.github.appspiriment.kolt.location.PlaceSearchResult

/** Hand-written fake per testing.md's fakes-over-mocks rule — no network, no platform APIs. */
internal class FakeLocationPickerGateway(
    private val searchResults: List<PlaceSearchResult> = emptyList(),
    private val mapPickResult: PickedMapLocation? = null,
    private val timezoneResult: String? = "Asia/Kolkata",
    private val currentLocationResult: LocationResult = LocationResult.PermissionDenied,
) : LocationPickerGateway {
    var searchCallCount = 0
        private set

    override suspend fun searchPlaces(query: String): List<PlaceSearchResult> {
        searchCallCount++
        return searchResults
    }

    override suspend fun resolveMapPick(latitude: Double, longitude: Double): PickedMapLocation =
        mapPickResult ?: PickedMapLocation(latitude, longitude, label = null, timezoneId = timezoneResult)

    override suspend fun resolveTimezone(latitude: Double, longitude: Double): String? = timezoneResult

    override suspend fun getCurrentLocation(): LocationResult = currentLocationResult
}
