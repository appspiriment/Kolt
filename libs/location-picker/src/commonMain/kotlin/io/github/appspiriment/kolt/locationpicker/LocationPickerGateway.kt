package io.github.appspiriment.kolt.locationpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.appspiriment.kolt.location.CurrentLocationProvider
import io.github.appspiriment.kolt.location.LocationResult
import io.github.appspiriment.kolt.location.PlaceSearchResult
import io.github.appspiriment.kolt.location.lookupTimezone as remoteLookupTimezone
import io.github.appspiriment.kolt.location.reverseGeocode as remoteReverseGeocode
import io.github.appspiriment.kolt.location.searchPlaces as remoteSearchPlaces

/**
 * Wraps everything [LocationPickerViewModel] needs from `libs/location` behind one small
 * interface. `CurrentLocationProvider` is a concrete `expect class` (not fakeable) and
 * `searchPlaces`/`reverseGeocode`/`lookupTimezone` are bare top-level suspend functions (also
 * not fakeable) as shipped there — this seam is what makes the ViewModel unit-testable per
 * testing.md's fakes-over-mocks rule; see `FakeLocationPickerGateway` in `commonTest`.
 */
internal interface LocationPickerGateway {
    suspend fun searchPlaces(query: String): List<PlaceSearchResult>
    suspend fun resolveMapPick(latitude: Double, longitude: Double): PickedMapLocation
    suspend fun resolveTimezone(latitude: Double, longitude: Double): String?
    suspend fun getCurrentLocation(): LocationResult
}

internal class DefaultLocationPickerGateway(
    private val locationProvider: CurrentLocationProvider,
) : LocationPickerGateway {
    override suspend fun searchPlaces(query: String): List<PlaceSearchResult> =
        remoteSearchPlaces(query)

    override suspend fun resolveMapPick(latitude: Double, longitude: Double): PickedMapLocation {
        val place = remoteReverseGeocode(latitude, longitude)
        val timezoneId = remoteLookupTimezone(latitude, longitude)
        return PickedMapLocation(
            latitude = latitude,
            longitude = longitude,
            label = place?.label?.substringBefore(","),
            timezoneId = timezoneId,
        )
    }

    override suspend fun resolveTimezone(latitude: Double, longitude: Double): String? =
        remoteLookupTimezone(latitude, longitude)

    override suspend fun getCurrentLocation(): LocationResult = locationProvider.getCurrentLocation()
}

/**
 * Reverse-geocodes + resolves a timezone for a tapped map point, in parallel. Also called
 * directly by each platform's `MapPickerContent` actual (map taps aren't routed through the
 * ViewModel/gateway — the platform map view resolves the pick itself before invoking its
 * `onPicked` callback), so this stays a plain top-level function rather than gateway-only.
 */
internal suspend fun resolveMapPick(latitude: Double, longitude: Double): PickedMapLocation {
    val place = remoteReverseGeocode(latitude, longitude)
    val timezoneId = remoteLookupTimezone(latitude, longitude)
    return PickedMapLocation(
        latitude = latitude,
        longitude = longitude,
        label = place?.label?.substringBefore(","),
        timezoneId = timezoneId,
    )
}

@Composable
internal fun rememberLocationPickerGateway(): LocationPickerGateway {
    val provider = rememberCurrentLocationProvider()
    return remember(provider) { DefaultLocationPickerGateway(provider) }
}
