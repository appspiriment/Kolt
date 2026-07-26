package io.github.appspiriment.kolt.locationpicker

/**
 * Per-platform entry point for launching the picker outside of embedding
 * [LocationPickerScreen] directly in your own composition:
 * - **Android**: `LocationPicker.rememberLauncher(config) { result -> ... }` (Compose), or use
 *   [LocationPickerContract] directly with `registerForActivityResult` from View-system code.
 * - **iOS**: `LocationPicker.present(fromViewController, config) { result -> ... }`.
 * - **Desktop**: `LocationPicker.showDialog(config) { result -> ... }` (call from a `@Composable`).
 * - **Wasm/Web**: `LocationPicker.Embed(config) { result -> ... }` (call from a `@Composable`).
 *
 * Each platform's actual object only exposes what makes sense for that platform — see the
 * platform-specific source sets for the actual signatures.
 */
expect object LocationPicker
