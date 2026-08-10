package io.github.appspiriment.kolt.location

/**
 * Opaque platform handle [CurrentLocationProvider] needs to do its work.
 * Android: wraps `android.content.Context` (construct with `PlatformLocationContext(context)`).
 * iOS/Desktop: empty, no handle required — construct with `PlatformLocationContext()`.
 */
expect class PlatformLocationContext

/**
 * Fetches a one-shot current-location reading.
 *
 * Platform behavior differs intentionally rather than being forced into one shape:
 * - **Android**: assumes the caller has already requested `ACCESS_FINE_LOCATION` /
 *   `ACCESS_COARSE_LOCATION` (e.g. via compose-utils' `rememberPermissionRequest`).
 *   Returns [LocationResult.PermissionDenied] if neither is granted.
 * - **iOS**: self-contained — requests `CLLocationManager` authorization itself if
 *   not yet determined, and returns [LocationResult.PermissionDenied] if declined.
 * - **Desktop**: no OS permission concept. Always attempts IP-based geolocation;
 *   callers should show their own consent UI first if desired.
 */
expect class CurrentLocationProvider(context: PlatformLocationContext) {
    suspend fun getCurrentLocation(): LocationResult
}

/**
 * Whether the device's location services (GPS/network positioning) are currently turned on —
 * distinct from app permission, which [CurrentLocationProvider] checks separately. Callers that
 * want to prompt the user to turn services on before fetching (e.g. location-picker's Android
 * "enable location services" bottomsheet) should check this first.
 *
 * - **Android**: `true` if the GPS or network provider is enabled.
 * - **iOS**: `CLLocationManager.locationServicesEnabled()`.
 * - **Desktop/Web**: always `true` — neither has an OS-level services toggle to check.
 */
expect fun isLocationServicesEnabled(context: PlatformLocationContext): Boolean
