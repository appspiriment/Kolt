package io.github.appspiriment.kolt.location

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority

/**
 * Android-only: resolves "location permission is granted but device location services are off"
 * via Play Services' `SettingsClient`, in-app — no navigation out to the OS Settings app.
 *
 * `checkLocationSettings()` fails with a [ResolvableApiException] carrying a `PendingIntent` when
 * services are off but resolvable; launching that shows the system's native "Turn on location"
 * bottom-sheet dialog without leaving the current `Activity`. The resolution result *is* the
 * answer — no separate poll of [isLocationServicesEnabled] needed on return.
 *
 * [isLocationServicesEnabled] remains the right tool for a synchronous, no-`Activity` check (e.g.
 * [CurrentLocationProvider.getCurrentLocation]'s suspend fast-fail path, which has no launcher to
 * show a resolution dialog with) — this Composable is the complementary "and let the user turn it
 * on in-app" step, additive rather than a replacement.
 *
 * @param onResolved Location settings already satisfied the request, or the user just enabled them via the system dialog.
 * @param onUnresolvable The failure wasn't a [ResolvableApiException] — e.g. a device/OEM with no way to resolve it automatically. [message] is the underlying reason, for display.
 * @param onDeclined The user dismissed the system "Turn on location" dialog without enabling it.
 * @return a `() -> Unit` — call it to trigger the check-then-resolve flow.
 */
@Composable
fun rememberLocationSettingsResolver(
    context: PlatformLocationContext,
    onResolved: () -> Unit,
    onUnresolvable: (message: String) -> Unit,
    onDeclined: () -> Unit,
): () -> Unit {
    val resolutionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) onResolved() else onDeclined()
    }

    return {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
        val settingsRequest = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).build()

        LocationServices.getSettingsClient(context.context)
            .checkLocationSettings(settingsRequest)
            .addOnSuccessListener { onResolved() }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    resolutionLauncher.launch(IntentSenderRequest.Builder(exception.resolution).build())
                } else {
                    onUnresolvable(exception.message ?: "Location unavailable")
                }
            }
    }
}
