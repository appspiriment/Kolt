package io.github.appspiriment.kolt.location

/**
 * A best-effort current-location reading. [label] and [timezoneId] are guesses
 * (reverse-geocoded city / device timezone) — callers should let the user confirm
 * or edit them before persisting.
 */
data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String? = null,
    val timezoneId: String? = null,
)

sealed interface LocationResult {
    data class Success(val location: GeoLocation) : LocationResult
    data object PermissionDenied : LocationResult
    data class Unavailable(val message: String) : LocationResult
}
