@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package io.github.appspiriment.kolt.location

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

// Hand-written bindings for the pieces of `navigator.geolocation` we need — kotlinx-browser
// doesn't expose the Geolocation API (confirmed against its issue tracker), so there's no
// existing library binding to reuse here.

private external interface GeolocationCoordinates : JsAny {
    val latitude: Double
    val longitude: Double
}

private external interface GeolocationPosition : JsAny {
    val coords: GeolocationCoordinates
}

// Per the W3C Geolocation API spec: 1 = PERMISSION_DENIED, 2 = POSITION_UNAVAILABLE, 3 = TIMEOUT.
private external interface GeolocationPositionError : JsAny {
    val code: Int
    val message: String
}

private external interface Geolocation : JsAny {
    fun getCurrentPosition(
        successCallback: (GeolocationPosition) -> Unit,
        errorCallback: (GeolocationPositionError) -> Unit,
    )
}

private external interface NavigatorWithGeolocation : JsAny {
    val geolocation: Geolocation?
}

private external val navigator: NavigatorWithGeolocation

actual class PlatformLocationContext

/** The browser has no separate services toggle — permission and services are the same prompt. */
actual fun isLocationServicesEnabled(context: PlatformLocationContext): Boolean = true

/**
 * Wraps the browser's `navigator.geolocation.getCurrentPosition` callback API. There is no
 * upfront permission check like Android's — calling `getCurrentPosition` is what triggers the
 * browser's own permission prompt.
 */
actual class CurrentLocationProvider actual constructor(context: PlatformLocationContext) {
    actual suspend fun getCurrentLocation(): LocationResult {
        val geolocation = navigator.geolocation
            ?: return LocationResult.Unavailable("This browser does not support geolocation")

        return suspendCancellableCoroutine { cont ->
            geolocation.getCurrentPosition(
                successCallback = { position ->
                    if (cont.isActive) {
                        cont.resume(
                            LocationResult.Success(
                                GeoLocation(
                                    latitude = position.coords.latitude,
                                    longitude = position.coords.longitude,
                                )
                            )
                        )
                    }
                },
                errorCallback = { error ->
                    if (cont.isActive) {
                        cont.resume(
                            when (error.code) {
                                1 -> LocationResult.PermissionDenied
                                else -> LocationResult.Unavailable(error.message)
                            }
                        )
                    }
                },
            )
        }
    }
}
