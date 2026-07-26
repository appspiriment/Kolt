package io.github.appspiriment.kolt.locationpicker

import androidx.lifecycle.viewModelScope
import io.github.appspiriment.kolt.location.LocationResult
import io.github.appspiriment.kolt.location.PlaceSearchResult
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 450L
private const val SEARCH_MIN_CHARS = 3

internal class LocationPickerViewModel(
    private val config: LocationPickerConfig,
    private val gateway: LocationPickerGateway,
) : MviViewModel<LocationPickerState, LocationPickerIntent, LocationPickerEffect>(
    LocationPickerState(selectedTab = config.defaultTab)
) {
    private var searchJob: Job? = null

    override fun onIntent(intent: LocationPickerIntent) {
        when (intent) {
            is LocationPickerIntent.SelectTab -> setState { copy(selectedTab = intent.tab, locationError = null) }
            is LocationPickerIntent.ChangeLabel -> setState { copy(label = intent.value) }
            is LocationPickerIntent.ChangeLatitude -> setState { copy(latitude = intent.value) }
            is LocationPickerIntent.ChangeLongitude -> setState { copy(longitude = intent.value) }
            is LocationPickerIntent.ChangeTimezone -> setState { copy(timezoneId = intent.value) }
            is LocationPickerIntent.SearchQueryChanged -> onSearchQueryChanged(intent.query)
            is LocationPickerIntent.SearchResultSelected -> onSearchResultSelected(intent.result)
            is LocationPickerIntent.MapPicked -> onMapPicked(intent.picked)
            LocationPickerIntent.TriggerCurrentLocation -> onTriggerCurrentLocation()
            LocationPickerIntent.Confirm -> onConfirm()
            LocationPickerIntent.Cancel -> sendEffect(LocationPickerEffect.Cancelled)
        }
    }

    private fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.length < SEARCH_MIN_CHARS) {
            setState { copy(placeQuery = query, placeResults = emptyList<PlaceSearchResult>().toPersistentList(), isSearchingPlaces = false) }
            return
        }
        setState { copy(placeQuery = query, placeResults = emptyList<PlaceSearchResult>().toPersistentList(), isSearchingPlaces = true) }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            val results = gateway.searchPlaces(query)
            setState { copy(placeResults = results.toPersistentList(), isSearchingPlaces = false) }
        }
    }

    private fun onSearchResultSelected(result: PlaceSearchResult) {
        setState {
            copy(
                label = result.label.substringBefore(","),
                latitude = result.latitude.toString(),
                longitude = result.longitude.toString(),
                placeQuery = result.label,
            )
        }
        resolveTimezoneThenReview(result.latitude, result.longitude)
    }

    private fun onMapPicked(picked: PickedMapLocation) {
        setState {
            copy(
                latitude = picked.latitude.toString(),
                longitude = picked.longitude.toString(),
                label = picked.label ?: label,
                locationError = null,
            )
        }
        if (picked.timezoneId != null) {
            setState { copy(timezoneId = picked.timezoneId, selectedTab = LocationPickerTab.MANUAL) }
        } else {
            resolveTimezoneThenReview(picked.latitude, picked.longitude)
        }
    }

    private fun resolveTimezoneThenReview(latitude: Double, longitude: Double) {
        setState { copy(isResolvingTimezone = true) }
        viewModelScope.launch {
            val timezoneId = gateway.resolveTimezone(latitude, longitude)
            if (timezoneId != null) {
                setState {
                    copy(
                        timezoneId = timezoneId,
                        isResolvingTimezone = false,
                        locationError = null,
                        selectedTab = LocationPickerTab.MANUAL,
                    )
                }
            } else {
                setState {
                    copy(
                        timezoneId = "",
                        isResolvingTimezone = false,
                        locationError = config.timezoneDetectFailedLabel,
                        selectedTab = LocationPickerTab.MANUAL,
                    )
                }
            }
        }
    }

    private fun onTriggerCurrentLocation() {
        setState { copy(isFetchingLocation = true, locationError = null) }
        viewModelScope.launch {
            when (val result = gateway.getCurrentLocation()) {
                is LocationResult.Success -> setState {
                    copy(
                        isFetchingLocation = false,
                        label = result.location.label ?: label,
                        latitude = result.location.latitude.toString(),
                        longitude = result.location.longitude.toString(),
                        timezoneId = result.location.timezoneId ?: timezoneId,
                        selectedTab = LocationPickerTab.MANUAL,
                    )
                }
                LocationResult.PermissionDenied -> setState {
                    copy(isFetchingLocation = false, locationError = config.permissionDeniedLabel)
                }
                is LocationResult.Unavailable -> setState {
                    copy(
                        isFetchingLocation = false,
                        locationError = "${config.locationUnavailableLabel}\n${result.message}",
                    )
                }
            }
        }
    }

    private fun onConfirm() {
        val current = state.value
        if (!current.isValid) return
        sendEffect(
            LocationPickerEffect.Confirmed(
                LocationPickerResult(
                    label = current.label.trim(),
                    latitude = current.latitude.toDouble(),
                    longitude = current.longitude.toDouble(),
                    timezoneId = current.timezoneId.trim(),
                )
            )
        )
    }
}
