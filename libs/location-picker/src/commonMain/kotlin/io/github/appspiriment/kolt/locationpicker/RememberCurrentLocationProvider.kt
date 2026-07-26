package io.github.appspiriment.kolt.locationpicker

import androidx.compose.runtime.Composable
import io.github.appspiriment.kolt.location.CurrentLocationProvider

/**
 * Builds a [CurrentLocationProvider] with whatever platform handle it needs (an Android
 * `Context`, nothing on iOS/Desktop) so [LocationPickerScreen] doesn't need to know the
 * difference.
 */
@Composable
expect fun rememberCurrentLocationProvider(): CurrentLocationProvider
