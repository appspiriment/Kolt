package io.github.appspiriment.kolt.locationpicker

import androidx.compose.runtime.Composable
import io.github.appspiriment.kolt.locationpicker.generated.resources.Res
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_back
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_cancel
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_confirm
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_current_tab
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_fetching_location
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_invalid_latitude
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_invalid_longitude
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_latitude_field
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_location_unavailable
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_longitude_field
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_manual_tab
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_map_hint
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_map_tab
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_name_field
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_permission_denied
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_permission_rationale_confirm
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_permission_rationale_dismiss
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_permission_rationale_message
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_permission_rationale_title
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_permission_settings_confirm
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_permission_settings_dismiss
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_permission_settings_message
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_permission_settings_title
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_resolving_timezone
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_services_disabled_confirm
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_services_disabled_dismiss
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_services_disabled_message
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_services_disabled_title
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_search_field
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_search_no_results
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_search_tab
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_searching
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_timezone_detect_failed
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_timezone_field
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_title
import io.github.appspiriment.kolt.locationpicker.generated.resources.location_picker_use_current_location
import org.jetbrains.compose.resources.stringResource

/**
 * The recommended way to build a [LocationPickerConfig]. Every label parameter defaults to
 * `null`, meaning "use the built-in English default from `composeResources/values/strings.xml`"
 * — pass a value only for the strings you actually want to override; everything else still
 * resolves via [stringResource] rather than falling back to a hardcoded literal. This is what
 * lets a caller override, say, just [confirmLabel] for their app's tone without having to
 * restate the other 20+ labels to keep them localized.
 *
 * [LocationPickerConfig]'s own constructor defaults are plain English literals instead — those
 * only apply at the two non-`@Composable` call sites that can't resolve a resource (see its
 * doc); every `@Composable` entry point in this module defaults to this function.
 */
@Composable
fun defaultLocationPickerConfig(
    showSearch: Boolean = true,
    showMap: Boolean = true,
    showCurrentLocation: Boolean = true,
    showManualEntry: Boolean = true,
    initialLatitude: Double = 20.5937,
    initialLongitude: Double = 78.9629,
    title: String? = null,
    searchTabLabel: String? = null,
    mapTabLabel: String? = null,
    currentLocationTabLabel: String? = null,
    manualTabLabel: String? = null,
    searchFieldLabel: String? = null,
    searchingLabel: String? = null,
    searchNoResultsLabel: String? = null,
    useCurrentLocationLabel: String? = null,
    fetchingLocationLabel: String? = null,
    resolvingTimezoneLabel: String? = null,
    mapPickerHint: String? = null,
    nameFieldLabel: String? = null,
    latitudeFieldLabel: String? = null,
    longitudeFieldLabel: String? = null,
    timezoneFieldLabel: String? = null,
    confirmLabel: String? = null,
    cancelLabel: String? = null,
    backLabel: String? = null,
    permissionDeniedLabel: String? = null,
    locationUnavailableLabel: String? = null,
    timezoneDetectFailedLabel: String? = null,
    invalidLatitudeLabel: String? = null,
    invalidLongitudeLabel: String? = null,
    locationPermissionRationaleTitle: String? = null,
    locationPermissionRationaleMessage: String? = null,
    locationPermissionRationaleConfirmLabel: String? = null,
    locationPermissionRationaleDismissLabel: String? = null,
    locationServicesDisabledTitle: String? = null,
    locationServicesDisabledMessage: String? = null,
    locationServicesDisabledConfirmLabel: String? = null,
    locationServicesDisabledDismissLabel: String? = null,
    locationPermissionSettingsTitle: String? = null,
    locationPermissionSettingsMessage: String? = null,
    locationPermissionSettingsConfirmLabel: String? = null,
    locationPermissionSettingsDismissLabel: String? = null,
): LocationPickerConfig = LocationPickerConfig(
    showSearch = showSearch,
    showMap = showMap,
    showCurrentLocation = showCurrentLocation,
    showManualEntry = showManualEntry,
    initialLatitude = initialLatitude,
    initialLongitude = initialLongitude,
    title = title ?: stringResource(Res.string.location_picker_title),
    searchTabLabel = searchTabLabel ?: stringResource(Res.string.location_picker_search_tab),
    mapTabLabel = mapTabLabel ?: stringResource(Res.string.location_picker_map_tab),
    currentLocationTabLabel = currentLocationTabLabel ?: stringResource(Res.string.location_picker_current_tab),
    manualTabLabel = manualTabLabel ?: stringResource(Res.string.location_picker_manual_tab),
    searchFieldLabel = searchFieldLabel ?: stringResource(Res.string.location_picker_search_field),
    searchingLabel = searchingLabel ?: stringResource(Res.string.location_picker_searching),
    searchNoResultsLabel = searchNoResultsLabel ?: stringResource(Res.string.location_picker_search_no_results),
    useCurrentLocationLabel = useCurrentLocationLabel
        ?: stringResource(Res.string.location_picker_use_current_location),
    fetchingLocationLabel = fetchingLocationLabel ?: stringResource(Res.string.location_picker_fetching_location),
    resolvingTimezoneLabel = resolvingTimezoneLabel
        ?: stringResource(Res.string.location_picker_resolving_timezone),
    mapPickerHint = mapPickerHint ?: stringResource(Res.string.location_picker_map_hint),
    nameFieldLabel = nameFieldLabel ?: stringResource(Res.string.location_picker_name_field),
    latitudeFieldLabel = latitudeFieldLabel ?: stringResource(Res.string.location_picker_latitude_field),
    longitudeFieldLabel = longitudeFieldLabel ?: stringResource(Res.string.location_picker_longitude_field),
    timezoneFieldLabel = timezoneFieldLabel ?: stringResource(Res.string.location_picker_timezone_field),
    confirmLabel = confirmLabel ?: stringResource(Res.string.location_picker_confirm),
    cancelLabel = cancelLabel ?: stringResource(Res.string.location_picker_cancel),
    backLabel = backLabel ?: stringResource(Res.string.location_picker_back),
    permissionDeniedLabel = permissionDeniedLabel ?: stringResource(Res.string.location_picker_permission_denied),
    locationUnavailableLabel = locationUnavailableLabel
        ?: stringResource(Res.string.location_picker_location_unavailable),
    timezoneDetectFailedLabel = timezoneDetectFailedLabel
        ?: stringResource(Res.string.location_picker_timezone_detect_failed),
    invalidLatitudeLabel = invalidLatitudeLabel ?: stringResource(Res.string.location_picker_invalid_latitude),
    invalidLongitudeLabel = invalidLongitudeLabel ?: stringResource(Res.string.location_picker_invalid_longitude),
    locationPermissionRationaleTitle = locationPermissionRationaleTitle
        ?: stringResource(Res.string.location_picker_permission_rationale_title),
    locationPermissionRationaleMessage = locationPermissionRationaleMessage
        ?: stringResource(Res.string.location_picker_permission_rationale_message),
    locationPermissionRationaleConfirmLabel = locationPermissionRationaleConfirmLabel
        ?: stringResource(Res.string.location_picker_permission_rationale_confirm),
    locationPermissionRationaleDismissLabel = locationPermissionRationaleDismissLabel
        ?: stringResource(Res.string.location_picker_permission_rationale_dismiss),
    locationServicesDisabledTitle = locationServicesDisabledTitle
        ?: stringResource(Res.string.location_picker_services_disabled_title),
    locationServicesDisabledMessage = locationServicesDisabledMessage
        ?: stringResource(Res.string.location_picker_services_disabled_message),
    locationServicesDisabledConfirmLabel = locationServicesDisabledConfirmLabel
        ?: stringResource(Res.string.location_picker_services_disabled_confirm),
    locationServicesDisabledDismissLabel = locationServicesDisabledDismissLabel
        ?: stringResource(Res.string.location_picker_services_disabled_dismiss),
    locationPermissionSettingsTitle = locationPermissionSettingsTitle
        ?: stringResource(Res.string.location_picker_permission_settings_title),
    locationPermissionSettingsMessage = locationPermissionSettingsMessage
        ?: stringResource(Res.string.location_picker_permission_settings_message),
    locationPermissionSettingsConfirmLabel = locationPermissionSettingsConfirmLabel
        ?: stringResource(Res.string.location_picker_permission_settings_confirm),
    locationPermissionSettingsDismissLabel = locationPermissionSettingsDismissLabel
        ?: stringResource(Res.string.location_picker_permission_settings_dismiss),
)
