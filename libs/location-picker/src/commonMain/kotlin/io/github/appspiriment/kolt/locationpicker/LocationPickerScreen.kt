package io.github.appspiriment.kolt.locationpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore

/**
 * All-in-one location picker: Search / Map / Current-location / Manual-entry tabs, whichever
 * [config] enables. Fills its [modifier]'s bounds — the caller supplies the container
 * (a full-screen [LocationPickerActivity] on Android, a `DialogWindow` on Desktop, a modal
 * `UIViewController` on iOS, or embedded inline anywhere else, including this composable used
 * directly inside an existing screen).
 *
 * This is the composable that owns the ViewModel and collects its effect `Flow` — the
 * presentation-mvi.md equivalent of a "Route" composable, just without an actual Navigation-3
 * back stack (this module has exactly one screen, launched by each platform's own mechanism
 * rather than a shared nav graph). [LocationPickerScreenContent] is the stateless, `@Preview`-able
 * renderer underneath.
 *
 * The ViewModel is owned by a manually-managed [ViewModelStore] rather than the usual
 * `androidx.lifecycle.viewmodel.compose.viewModel { }` factory: that factory needs a
 * `LocalViewModelStoreOwner` in scope, which Android's `ComponentActivity` provides for free but
 * Desktop's `DialogWindow`, iOS's `ComposeUIViewController`, and Web's `ComposeViewport` don't —
 * this module is embedded into all four without controlling what hosts it.
 * `ViewModel.clear()` itself is internal (framework-only) — `ViewModelStore.put`/`.get`/`.clear()`
 * are the public, documented API for exactly this manual/non-Android-owned case, and `clear()`
 * on the store is what triggers each held ViewModel's `viewModelScope` to cancel.
 */
@Composable
fun LocationPickerScreen(
    config: LocationPickerConfig = defaultLocationPickerConfig(),
    modifier: Modifier = Modifier,
    colors: LocationPickerColors? = null,
    onCancel: () -> Unit,
    onResult: (LocationPickerResult) -> Unit,
) {
    val gateway = rememberLocationPickerGateway()
    val viewModelStore = remember { ViewModelStore() }
    DisposableEffect(viewModelStore) { onDispose { viewModelStore.clear() } }
    val viewModel = remember(viewModelStore, config) {
        (viewModelStore.get("locationPicker") as? LocationPickerViewModel)
            ?: LocationPickerViewModel(config, gateway).also { viewModelStore.put("locationPicker", it) }
    }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LocationPickerEffect.Confirmed -> onResult(effect.result)
                LocationPickerEffect.Cancelled -> onCancel()
            }
        }
    }

    LocationPickerScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        config = config,
        modifier = modifier,
        colors = colors,
    )
}

/**
 * The stateless root composable — reads only [state], reports only [LocationPickerIntent]s, and
 * has no ViewModel/effect-`Flow` awareness, per presentation-mvi.md. `expect` because its
 * implementation reuses `compose-kmp`'s theme + components on Android/iOS/Desktop (mandatory
 * reuse per kolt-libs.md) but can't on Web — compose-kmp has no wasmJs target, and this module's
 * wasmJs target still needs to build. See `LocationPickerScreenContent.wasmJs.kt` for that
 * documented exception.
 *
 * [colors]: when non-null, every renderer overrides its ambient theme with it (compose-kmp's
 * `LocalColors` on Android/iOS/Desktop, `MaterialTheme.colorScheme` on Web) so the whole screen —
 * including compose-kmp's own `AppsButton`/`AppsValidatedTextField`/`AppsCircularProgress`, which
 * read `Kolt.colors` internally with no per-call override — actually reflects it. `null` (the
 * default) leaves each platform's ambient theme alone.
 */
@Composable
internal expect fun LocationPickerScreenContent(
    state: LocationPickerState,
    onIntent: (LocationPickerIntent) -> Unit,
    config: LocationPickerConfig,
    modifier: Modifier,
    colors: LocationPickerColors?,
)
