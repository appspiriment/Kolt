package io.github.appspiriment.kolt.locationpicker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.github.appspiriment.kolt.composekmp.components.core.VerticalSpacer
import io.github.appspiriment.kolt.composekmp.components.core.buttons.AppsButton
import io.github.appspiriment.kolt.composekmp.components.core.buttons.AppsTextButton
import io.github.appspiriment.kolt.composekmp.components.core.messages.AppsBottomSheet
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText
import io.github.appspiriment.kolt.location.PlatformLocationContext
import io.github.appspiriment.kolt.location.rememberLocationSettingsResolver

private val LOCATION_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

/**
 * Android-only gate in front of [LocationPickerIntent.TriggerCurrentLocation]: checks location
 * permission first (showing [config]'s rationale bottomsheet before the system prompt, since
 * `libs/location`'s Android [io.github.appspiriment.kolt.location.CurrentLocationProvider]
 * assumes the caller already requested it — see its doc), falling back to a bottomsheet
 * deep-linking to app settings if the permission was already permanently denied ("Don't ask
 * again" — the system prompt won't reappear for that). Once granted, resolves the device's
 * location settings via `libs/location`'s [rememberLocationSettingsResolver] — the common,
 * resolvable case shows Play Services' in-app "Turn on location" system dialog directly, no
 * bottomsheet of ours first; the [config]'s "services disabled" bottomsheet is only a fallback
 * for the rare unresolvable case (falls back to a Settings deep-link). Only once all of that
 * clears does it call [onGranted]. iOS/Desktop don't need this: iOS's `CurrentLocationProvider`
 * requests its own permission, and Desktop has no permission/services concept — so this has no
 * expect/actual counterpart, it's wired in directly by [LocationPickerScreenContent]'s Android
 * actual.
 *
 * Returns a `() -> Unit` to wire to the "use current location" trigger in place of dispatching
 * the intent directly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberLocationAccessGate(config: LocationPickerConfig, onGranted: () -> Unit): () -> Unit {
    val context = LocalContext.current
    val activity = context as? Activity
    var showRationale by remember { mutableStateOf(false) }
    var showPermissionSettings by remember { mutableStateOf(false) }
    var showEnableServices by remember { mutableStateOf(false) }

    fun hasPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    /** True once the user has denied without checking "don't ask again" — i.e. asking again is still useful. */
    fun canShowSystemRationale() = activity != null &&
        LOCATION_PERMISSIONS.any { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }

    // checkLocationSettings() (inside the resolver) already tells us "is it on" — no separate
    // isLocationServicesEnabled() pre-check needed before calling it.
    val resolveLocationSettings = rememberLocationSettingsResolver(
        context = PlatformLocationContext(context),
        onResolved = onGranted,
        onUnresolvable = { showEnableServices = true },
        onDeclined = {}, // user dismissed the system dialog — tapping the trigger again retries
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        when {
            results.values.any { it } -> resolveLocationSettings()
            canShowSystemRationale() -> Unit // user can retry — tapping the trigger again re-shows the rationale sheet
            else -> showPermissionSettings = true // denied with "don't ask again" (or never determined pre-request)
        }
    }
    val appSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (hasPermission()) resolveLocationSettings()
    }
    val enableServicesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Fallback path only (unresolvable device) — re-run the resolver rather than trusting a
        // raw isLocationServicesEnabled() poll, since Play Services is the source of truth here.
        resolveLocationSettings()
    }

    AccessBottomSheet(
        show = showRationale,
        title = config.locationPermissionRationaleTitle.toUiText(),
        message = config.locationPermissionRationaleMessage.toUiText(),
        confirmLabel = config.locationPermissionRationaleConfirmLabel.toUiText(),
        dismissLabel = config.locationPermissionRationaleDismissLabel.toUiText(),
        onDismiss = { showRationale = false },
        onConfirm = {
            showRationale = false
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        },
    )

    AccessBottomSheet(
        show = showPermissionSettings,
        title = config.locationPermissionSettingsTitle.toUiText(),
        message = config.locationPermissionSettingsMessage.toUiText(),
        confirmLabel = config.locationPermissionSettingsConfirmLabel.toUiText(),
        dismissLabel = config.locationPermissionSettingsDismissLabel.toUiText(),
        onDismiss = { showPermissionSettings = false },
        onConfirm = {
            showPermissionSettings = false
            appSettingsLauncher.launch(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
            )
        },
    )

    AccessBottomSheet(
        show = showEnableServices,
        title = config.locationServicesDisabledTitle.toUiText(),
        message = config.locationServicesDisabledMessage.toUiText(),
        confirmLabel = config.locationServicesDisabledConfirmLabel.toUiText(),
        dismissLabel = config.locationServicesDisabledDismissLabel.toUiText(),
        onDismiss = { showEnableServices = false },
        onConfirm = {
            showEnableServices = false
            enableServicesLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        },
    )

    return {
        when {
            hasPermission() -> resolveLocationSettings()
            else -> showRationale = true
        }
    }
}

/** Shared two-button bottomsheet body for [rememberLocationAccessGate]'s three prompts. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccessBottomSheet(
    show: Boolean,
    title: UiText,
    message: UiText,
    confirmLabel: UiText,
    dismissLabel: UiText,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!show) return
    AppsBottomSheet(
        showSheet = true,
        state = rememberModalBottomSheetState(),
        title = title,
        dismissSheet = onDismiss,
    ) {
        AppspirimentText(text = message, color = Kolt.colors.subText)
        VerticalSpacer()
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
            AppsTextButton(text = dismissLabel, onClick = onDismiss)
            AppsButton(text = confirmLabel, onClick = onConfirm)
        }
    }
}
