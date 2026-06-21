package io.github.appspiriment.kolt.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLGeocoder
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.CLPlacemark
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import kotlinx.datetime.TimeZone
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class PlatformLocationContext

/**
 * Self-contained: requests `CLLocationManager` "when in use" authorization itself
 * (iOS bundles the permission prompt into the location-request flow), then fetches
 * one location fix and reverse-geocodes it.
 */
actual class CurrentLocationProvider actual constructor(
    private val context: PlatformLocationContext
) {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getCurrentLocation(): LocationResult {
        val manager = CLLocationManager()

        if (!ensureAuthorized(manager)) return LocationResult.PermissionDenied

        val location = requestLocationOnce(manager)
            ?: return LocationResult.Unavailable("Could not determine location")

        val placemark = reverseGeocode(location)
        val (latitude, longitude) = location.coordinate.useContents { latitude to longitude }

        return LocationResult.Success(
            GeoLocation(
                latitude = latitude,
                longitude = longitude,
                label = placemark?.locality ?: placemark?.administrativeArea ?: placemark?.name,
                timezoneId = placemark?.timeZone?.name ?: TimeZone.currentSystemDefault().id,
            )
        )
    }

    private suspend fun ensureAuthorized(manager: CLLocationManager): Boolean {
        val status = CLLocationManager.authorizationStatus()
        if (isAuthorized(status)) return true
        if (status == kCLAuthorizationStatusDenied || status == kCLAuthorizationStatusRestricted) return false

        return suspendCancellableCoroutine { cont ->
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                    val newStatus = CLLocationManager.authorizationStatus()
                    if (newStatus != kCLAuthorizationStatusNotDetermined) {
                        manager.delegate = null
                        if (cont.isActive) cont.resume(isAuthorized(newStatus))
                    }
                }
            }
            manager.delegate = delegate
            manager.requestWhenInUseAuthorization()
        }
    }

    private fun isAuthorized(status: CLAuthorizationStatus): Boolean =
        status == kCLAuthorizationStatusAuthorizedAlways || status == kCLAuthorizationStatusAuthorizedWhenInUse

    private suspend fun requestLocationOnce(manager: CLLocationManager): CLLocation? =
        suspendCancellableCoroutine { cont ->
            val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
                override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
                    manager.delegate = null
                    val location = didUpdateLocations.lastOrNull() as? CLLocation
                    if (cont.isActive) cont.resume(location)
                }

                override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
                    manager.delegate = null
                    if (cont.isActive) cont.resume(null)
                }
            }
            manager.delegate = delegate
            manager.requestLocation()
            cont.invokeOnCancellation { manager.delegate = null }
        }

    private suspend fun reverseGeocode(location: CLLocation): CLPlacemark? =
        suspendCancellableCoroutine { cont ->
            CLGeocoder().reverseGeocodeLocation(location) { placemarks, _ ->
                if (cont.isActive) cont.resume((placemarks as? List<*>)?.firstOrNull() as? CLPlacemark)
            }
        }
}
