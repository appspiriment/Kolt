package io.github.appspiriment.kolt.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume

actual class PlatformLocationContext(val context: Context)

/**
 * Assumes the caller has already requested `ACCESS_FINE_LOCATION` /
 * `ACCESS_COARSE_LOCATION` (e.g. via compose-utils' `rememberPermissionRequest`) —
 * this class only checks current grant status, it does not request permission itself.
 */
actual class CurrentLocationProvider actual constructor(
    context: PlatformLocationContext
) {
    private val context: Context = context.context

    actual suspend fun getCurrentLocation(): LocationResult {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return LocationResult.PermissionDenied

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val provider = when {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> return LocationResult.Unavailable("Location services are turned off")
        }

        val location = requestSingleLocation(locationManager, provider)
            ?: return LocationResult.Unavailable("Could not determine location")

        val label = withContext(Dispatchers.IO) { reverseGeocode(location.latitude, location.longitude) }
        return LocationResult.Success(
            GeoLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                label = label,
                timezoneId = TimeZone.getDefault().id,
            )
        )
    }

    @Suppress("DEPRECATION")
    private suspend fun requestSingleLocation(
        locationManager: LocationManager,
        provider: String
    ): Location? = suspendCancellableCoroutine { cont ->
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                if (cont.isActive) cont.resume(location)
            }
        }
        try {
            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        } catch (e: SecurityException) {
            if (cont.isActive) cont.resume(null)
        }
        cont.invokeOnCancellation { locationManager.removeUpdates(listener) }
    }

    @Suppress("DEPRECATION")
    private fun reverseGeocode(latitude: Double, longitude: Double): String? = try {
        Geocoder(context, Locale.getDefault())
            .getFromLocation(latitude, longitude, 1)
            ?.firstOrNull()
            ?.let { it.locality ?: it.subAdminArea ?: it.adminArea }
    } catch (e: Exception) {
        null
    }
}
