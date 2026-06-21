package io.github.appspiriment.kolt.composeutils.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.appspiriment.kolt.composeutils.R
import io.github.appspiriment.kolt.composekmp.components.core.VerticalSpacer
import io.github.appspiriment.kolt.composekmp.components.core.buttons.AppsButton
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.components.core.messages.DialogButtonStyle
import io.github.appspiriment.kolt.composekmp.components.core.messages.MessageDialog
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.CompositionBaseProvider
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText
import io.github.appspiriment.kolt.composekmp.wrappers.uiTextResource

//==============================================================================================
// 1. Core Data Models & State Holders
//==============================================================================================

/**
 * Represents the current status of a runtime permission.
 */
enum class PermissionStatus {
    /** The permission has been granted by the user. */
    GRANTED,

    /** The permission has been denied, but can be requested again. A rationale should be shown. */
    DENIED,

    /** The permission has been denied and the user has selected "Don't ask again". */
    DENIED_PERMANENTLY,

    /** The status is currently being checked. */
    CHECKING
}

/**
 * Defines a permission required by the application.
 *
 * @param permission The permission string, e.g., `android.Manifest.permission.CAMERA`.
 * @param name A user-friendly name for the permission, e.g., "Camera". Used in default rationale messages.
 * @param isRequired If `true`, this permission is considered critical for an action to proceed.
 *                   The `onActionGranted` callback in [rememberPermissionRequest] will only be called
 *                   if all `isRequired` permissions are granted.
 */
data class AppPermission(
    val permission: String,
    val name: String,
    val isRequired: Boolean = true
)

/**
 * An internal state holder for a single permission.
 */
internal class SinglePermissionState(
    val appPermission: AppPermission
) {
    var status by mutableStateOf(PermissionStatus.CHECKING)

    fun updateStatus(activity: Activity) {
        val permissionStatus = ContextCompat.checkSelfPermission(activity, appPermission.permission)
        status = when {
            permissionStatus == PackageManager.PERMISSION_GRANTED -> PermissionStatus.GRANTED
            ActivityCompat.shouldShowRequestPermissionRationale(activity, appPermission.permission) -> PermissionStatus.DENIED
            else -> {
                // This case is hit on first launch (before request) and after permanent denial.
                // We must default to DENIED to allow the first request to proceed.
                // The DENIED_PERMANENTLY state will be correctly set by handlePermissionsResult
                // after a request is attempted and fails to show a dialog.
                PermissionStatus.DENIED
            }
        }
    }
}

/**
 * A state holder that manages the status of multiple permissions.
 * This is the object returned by [rememberAppPermissionsState] and is the core of the permission utility.
 *
 * @param permissionsDefinition The initial list of [AppPermission] to manage.
 */
class AppPermissionsOverallState internal constructor(
    val permissionsDefinition: List<AppPermission>
) {
    internal var permissionStates by mutableStateOf(permissionsDefinition.map { SinglePermissionState(it) })
        private set

    internal lateinit var launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>

    /** `true` if all permissions marked as `isRequired` have been granted. */
    val allRequiredPermissionsGranted: Boolean
        get() = permissionStates.filter { it.appPermission.isRequired }.all { it.status == PermissionStatus.GRANTED }

    /** `true` if the status of any permission is currently being checked. */
    val isChecking: Boolean
        get() = permissionStates.any { it.status == PermissionStatus.CHECKING }

    /** `true` if any permission has been permanently denied by the user. */
    val hasPermanentlyDeniedPermissions: Boolean
        get() = permanentlyDeniedPermissions.isNotEmpty()

    /** A list of permissions that have been denied but can be requested again. */
    val permissionsNeedingRationale: List<AppPermission>
        get() = permissionStates.filter { it.status == PermissionStatus.DENIED }.map { it.appPermission }

    /** A list of permissions that have been permanently denied. */
    val permanentlyDeniedPermissions: List<AppPermission>
        get() = permissionStates.filter { it.status == PermissionStatus.DENIED_PERMANENTLY }.map { it.appPermission }

    /** A list of all permissions that have not been granted. */
    val allDeniedPermissions: List<AppPermission>
        get() = permissionStates.filter { it.status != PermissionStatus.GRANTED }.map { it.appPermission }

    internal fun registerLauncher(l: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>) {
        launcher = l
    }

    internal fun updateAllStatuses(activity: Activity) {
        permissionStates.forEach { it.updateStatus(activity) }
        permissionStates = permissionStates.toList() // Trigger recomposition
    }

    /**
     * Launches the system permission request dialog for all permissions that are currently in the [PermissionStatus.DENIED] state.
     */
    fun requestPermissions() {
        val permissionsToRequest = permissionStates
            .filter { it.status == PermissionStatus.DENIED }
            .map { it.appPermission.permission }
            .toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            launcher.launch(permissionsToRequest)
        }
    }

    internal fun handlePermissionsResult(activity: Activity, grantResults: Map<String, Boolean>) {
        grantResults.forEach { (permissionString, isGranted) ->
            permissionStates.find { it.appPermission.permission == permissionString }?.let { state ->
                state.status = if (isGranted) {
                    PermissionStatus.GRANTED
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionString)) {
                        PermissionStatus.DENIED
                    } else {
                        PermissionStatus.DENIED_PERMANENTLY
                    }
                }
            }
        }
        updateAllStatuses(activity)
    }

    /**
     * Opens the application's settings screen, allowing the user to manually grant permissions.
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

//==============================================================================================
// 2. Composable Permission Hooks
//==============================================================================================

/**
 * The base hook for managing permission state in Compose. It tracks the status of a list of
 * permissions and provides methods to request them. This is the low-level building block for
 * creating custom permission handling logic.
 *
 * @param permissionsList The list of [AppPermission] to manage.
 * @return An [AppPermissionsOverallState] object that holds the current state and provides methods for interaction.
 */
@Composable
fun rememberAppPermissionsState(permissionsList: List<AppPermission>): AppPermissionsOverallState {
    val context = LocalContext.current
    val activity = context as? Activity ?: throw IllegalStateException("Permissions handler needs an Activity context")

    val state = remember(permissionsList) { AppPermissionsOverallState(permissionsList) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
        state.handlePermissionsResult(activity, grantResults)
    }

    LaunchedEffect(permissionsList, activity) {
        state.registerLauncher(launcher)
        state.updateAllStatuses(activity) // Initial check
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, state, activity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (!state.allRequiredPermissionsGranted) {
                    state.updateAllStatuses(activity)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    return state
}

/**
 * A high-level composable hook that simplifies the entire permission request workflow for a single action.
 *
 * This hook manages checking permissions, showing rationale dialogs, requesting permissions,
 * and handling permanently denied cases. It returns a single launcher function that you can
 * attach to a button's `onClick` to trigger the process.
 *
 * @param permissions The list of [AppPermission] objects required for the action.
 * @param onActionGranted The lambda to be executed when all `isRequired` permissions are successfully granted.
 * @param askFirst If true, a rationale dialog will be shown *before* the system permission prompt is requested.
 * @param rationaleMessage A lambda that returns a [UiText] for the initial rationale dialog. It receives the list of permissions needing rationale.
 * @param rationaleTitle The title for the initial rationale dialog.
 * @param rationaleConfirmText The text for the confirm button in the rationale dialog.
 * @param rationaleDismissText The text for the dismiss button in the rationale dialog.
 * @param settingsMessage A lambda that returns a [UiText] for the permanently denied dialog. It receives the list of permanently denied permissions.
 * @param settingsTitle The title for the permanently denied dialog.
 * @param settingsConfirmText The text for the confirm button in the settings dialog.
 * @param settingsDismissText The text for the dismiss button in the settings dialog.
 * @param dialogButtonStyle The visual style for the dialog buttons (e.g., stacked or transparent).
 * @return A launcher lambda `() -> Unit` that you should call to initiate the permission request flow.
 */
@Composable
fun rememberPermissionRequest(
    permissions: List<AppPermission>,
    onActionGranted: () -> Unit,
    askFirst: Boolean = false,
    // Rationale dialog texts and logic
    rationaleMessage: (List<AppPermission>) -> UiText = {
        val message = "To proceed, please grant the following permissions:\n\n" +
                it.joinToString(separator = "\n") { perm -> "● ${perm.name}" }
        message.toUiText()
    },
    rationaleTitle: UiText = uiTextResource(R.string.permission_required),
    rationaleConfirmText: UiText = uiTextResource(R.string.grant_permission),
    rationaleDismissText: UiText = uiTextResource(R.string.cancel),
    // Settings dialog texts and logic
    settingsMessage: (List<AppPermission>) -> UiText = {
        val message = "You have permanently denied permissions required for this feature. Please enable them in app settings:\n\n" +
                it.joinToString(separator = "\n") { perm -> "● ${perm.name}" }
        message.toUiText()
    },
    settingsTitle: UiText = uiTextResource(R.string.permission_required),
    settingsConfirmText: UiText = uiTextResource(R.string.open_settings),
    settingsDismissText: UiText = uiTextResource(R.string.cancel),
    dialogButtonStyle: DialogButtonStyle = DialogButtonStyle.transparent()
): () -> Unit {

    val context = LocalContext.current
    val permissionState = rememberAppPermissionsState(permissions)

    var currentRationaleMessage by remember { mutableStateOf<UiText?>(null) }
    var currentSettingsMessage by remember { mutableStateOf<UiText?>(null) }

    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var actionPending by remember { mutableStateOf(false) }

    LaunchedEffect(permissionState.allRequiredPermissionsGranted, actionPending) {
        if (permissionState.allRequiredPermissionsGranted && actionPending) {
            onActionGranted()
            actionPending = false
        }
    }

    val launcher: () -> Unit = {
        when {
            permissionState.allRequiredPermissionsGranted -> onActionGranted()
            permissionState.hasPermanentlyDeniedPermissions -> {
                currentSettingsMessage = settingsMessage(permissionState.permanentlyDeniedPermissions)
                showSettingsDialog = true
            }
            askFirst -> {
                currentRationaleMessage = rationaleMessage(permissionState.permissionsNeedingRationale.ifEmpty { permissions })
                showRationaleDialog = true
            }
            else -> {
                actionPending = true
                permissionState.requestPermissions()
            }
        }
    }

    if (showRationaleDialog) {
        MessageDialog(
            title = rationaleTitle,
            message = currentRationaleMessage,
            positiveText = rationaleConfirmText,
            negativeText = rationaleDismissText,
            buttonStyle = dialogButtonStyle,
            onDismissRequest = { showRationaleDialog = false },
            listener = { confirmed ->
                showRationaleDialog = false
                if (confirmed) {
                    actionPending = true
                    permissionState.requestPermissions()
                }
            }
        )
    }

    if (showSettingsDialog) {
        MessageDialog(
            title = settingsTitle,
            message = currentSettingsMessage,
            positiveText = settingsConfirmText,
            negativeText = settingsDismissText,
            buttonStyle = dialogButtonStyle,
            onDismissRequest = { showSettingsDialog = false },
            listener = { confirmed ->
                showSettingsDialog = false
                if (confirmed) {
                    actionPending = true
                    permissionState.openAppSettings(context)
                }
            }
        )
    }

    return launcher
}

//==============================================================================================
// 3. Full-Screen Permission Handlers
//==============================================================================================

/**
 * A container that manages permission states and provides callbacks for different outcomes.
 * This is a lower-level building block for creating custom permission UIs.
 *
 * @param permissions The list of [AppPermission] to manage.
 * @param onAllPermissionsGranted A composable lambda executed when all required permissions are granted.
 * @param onPermissionsNotGranted A composable lambda executed when one or more permissions are not granted.
 * @param onChecking A composable lambda executed while the permission status is being checked.
 */
@Composable
fun PermissionsManagerContainer(
    permissions: List<AppPermission>,
    onAllPermissionsGranted: @Composable (state: AppPermissionsOverallState) -> Unit,
    onPermissionsNotGranted: @Composable (
        allDenied: List<AppPermission>,
        permanentlyDenied: List<AppPermission>,
        deniedForRationale: List<AppPermission>,
        requestPermissions: () -> Unit,
        openSettings: () -> Unit,
        state: AppPermissionsOverallState
    ) -> Unit,
    onChecking: @Composable () -> Unit = {}
) {
    val context = LocalContext.current
    val permissionsState = rememberAppPermissionsState(permissionsList = permissions)

    when {
        permissionsState.isChecking -> onChecking()
        permissionsState.allRequiredPermissionsGranted -> onAllPermissionsGranted(permissionsState)
        else -> {
            onPermissionsNotGranted(
                permissionsState.allDeniedPermissions,
                permissionsState.permanentlyDeniedPermissions,
                permissionsState.permissionsNeedingRationale,
                permissionsState::requestPermissions,
                { permissionsState.openAppSettings(context) },
                permissionsState
            )
        }
    }
}

/**
 * A high-level container that handles permissions and displays the provided `content` only when
 * all required permissions are granted. Otherwise, it shows a default full-screen UI for the user
 * to grant the necessary permissions.
 *
 * @param permissions The list of [AppPermission] required for the `content` to be shown.
 * @param content The composable content to display once all permissions are granted.
 */
@Composable
fun PermissionHandler(
    permissions: List<AppPermission>,
    content: @Composable () -> Unit
) {
    PermissionsManagerContainer(
        permissions = permissions,
        onAllPermissionsGranted = { _ -> content() },
        onPermissionsNotGranted = { _, permanentlyDenied, deniedForRationale, requestPermissions, openSettings, _ ->
            DefaultPermissionsRequestUI(
                permanentlyDenied = permanentlyDenied,
                needingRationale = deniedForRationale,
                onRequestPermissions = requestPermissions,
                onOpenSettings = openSettings,
                modifier = Modifier.fillMaxSize()
            )
        },
    )
}

/**
 * A composable that acts as a gatekeeper for its content. It displays the `content` only when
 * all required permissions are granted. If permissions are not granted, it shows a rationale
 * dialog, prompting the user to grant them.
 *
 * This is ideal for gating an entire screen or a critical piece of UI that cannot function
 * without specific permissions.
 *
 * @param permissions The list of [AppPermission] required for the `content`.
 * @param onDismiss A callback invoked if the user cancels the permission request dialog.
 * @param rationaleMessage A lambda that returns the [UiText] to show in the rationale dialog.
 * @param settingsMessage A lambda that returns the [UiText] to show in the settings dialog for permanently denied permissions.
 * @param content The composable content to be displayed once all permissions are granted.
 */
@Composable
fun PermissionRequestDialog(
    permissions: List<AppPermission>,
    onDismiss: () -> Unit,
    rationaleMessage: (List<AppPermission>) -> UiText = {
        val message = "To access this feature, please grant the following permissions:\n\n" +
                it.joinToString(separator = "\n") { perm -> "● ${perm.name}" }
        message.toUiText()
    },
    settingsMessage: (List<AppPermission>) -> UiText = {
        val message = "You have permanently denied permissions required for this feature. Please enable them in app settings:\n\n" +
                it.joinToString(separator = "\n") { perm -> "● ${perm.name}" }
        message.toUiText()
    },
    content: @Composable () -> Unit
) {
    var hasBeenDismissed by remember { mutableStateOf(false) }

    PermissionsManagerContainer(
        permissions = permissions,
        onAllPermissionsGranted = { content() },
        onPermissionsNotGranted = { _, permanentlyDenied, rationale, requestPermissions, openSettings, _ ->
            if (!hasBeenDismissed) {
                val isPermanentlyDenied = permanentlyDenied.isNotEmpty()

                MessageDialog(
                    title = uiTextResource(R.string.permission_required),
                    message = if (isPermanentlyDenied) settingsMessage(permanentlyDenied) else rationaleMessage(rationale.ifEmpty { permissions }),
                    positiveText = if (isPermanentlyDenied) uiTextResource(R.string.open_settings) else uiTextResource(R.string.grant_permission),
                    negativeText = uiTextResource(R.string.cancel),
                    listener = { confirmed ->
                        if (confirmed) {
                            if (isPermanentlyDenied) openSettings() else requestPermissions()
                        } else {
                            hasBeenDismissed = true
                            onDismiss()
                        }
                    },
                    onDismissRequest = {
                        hasBeenDismissed = true
                        onDismiss()
                    }
                )
            }
        }
    )
}


/**
 * The default full-screen UI for guiding the user to grant permissions. It intelligently
 * differentiates between permissions that have been permanently denied and those that can
 * still be requested, showing the appropriate message and button for each case.
 *
 * @param permanentlyDenied A list of permissions the user has permanently denied.
 * @param needingRationale A list of permissions that can be requested again.
 * @param onRequestPermissions A lambda to trigger the system permission request.
 * @param onOpenSettings A lambda to open the app's settings screen.
 * @param modifier A modifier for the container.
 * @param title The title text to display.
 * @param permanentlyDeniedMessage The message to show when permissions are permanently denied.
 * @param openSettingsButtonText The text for the button that opens settings.
 * @param rationaleMessage The message to show when permissions can be requested.
 * @param grantPermissionButtonText The text for the button that requests permissions.
 * @param genericMessage A fallback message for initial or unclear states.
 * @param permissionItemContent A lambda to customize the rendering of each permission in the list.
 */
@Composable
fun DefaultPermissionsRequestUI(
    permanentlyDenied: List<AppPermission>,
    needingRationale: List<AppPermission>,
    onRequestPermissions: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    title: UiText = uiTextResource(R.string.permissions_required_title),
    permanentlyDeniedMessage: UiText = uiTextResource(R.string.permissions_denied_permanantly_message),
    openSettingsButtonText: UiText = uiTextResource(R.string.open_settings),
    rationaleMessage: UiText = uiTextResource(R.string.permissions_needed_message),
    grantPermissionButtonText: UiText = uiTextResource(R.string.grant_permission),
    genericMessage: UiText = uiTextResource(R.string.permissions_generic_title),
    permissionItemContent: @Composable (AppPermission) -> Unit = {
        AppspirimentText(text = "● ${it.name}", color = Kolt.colors.accentedBlueText)
    }
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Kolt.sizes.paddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppspirimentText(
                text = title,
                style = Kolt.typography.textMediumLarge.semiBold
            )
            VerticalSpacer(height = Kolt.sizes.paddingSmall)
            val allDeniedOrNeedingRationale = (permanentlyDenied + needingRationale).distinctBy { it.permission }

            // Render the list of permissions using the provided lambda
            allDeniedOrNeedingRationale.forEach {
                permissionItemContent(it)
            }

            VerticalSpacer(height = Kolt.sizes.paddingSmall)

            // Show the appropriate message and button based on the state
            if (permanentlyDenied.isNotEmpty()) {
                AppspirimentText(
                    text = permanentlyDeniedMessage,
                    color = Kolt.colors.subText,
                    textAlign = TextAlign.Center
                )
                VerticalSpacer(height = Kolt.sizes.paddingLarge)
                AppsButton(openSettingsButtonText) { onOpenSettings() }
            } else if (allDeniedOrNeedingRationale.isNotEmpty()) {
                AppspirimentText(
                    text = rationaleMessage,
                    color = Kolt.colors.subText,
                    textAlign = TextAlign.Center
                )
                VerticalSpacer()
                AppsButton(grantPermissionButtonText) { onRequestPermissions() }
            } else {
                AppspirimentText(text = genericMessage)
                VerticalSpacer(height = Kolt.sizes.paddingSmall)
                VerticalSpacer()
                AppsButton(grantPermissionButtonText) { onRequestPermissions() }
            }
        }
    }
}

@Preview(showBackground = true, name = "Default UI - Rationale State")
@Composable
private fun DefaultPermissionsRequestUI_Rationale_Preview() {
    val permissions = listOf(
        AppPermission("android.permission.CAMERA", "Camera"),
        AppPermission("android.permission.RECORD_AUDIO", "Microphone")
    )
    CompositionBaseProvider {
        Surface {
            DefaultPermissionsRequestUI(
                permanentlyDenied = emptyList(),
                needingRationale = permissions,
                onRequestPermissions = {},
                onOpenSettings = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Default UI - Permanently Denied State")
@Composable
private fun DefaultPermissionsRequestUI_PermanentlyDenied_Preview() {
    val permissions = listOf(
        AppPermission("android.permission.CAMERA", "Camera"),
        AppPermission("android.permission.ACCESS_FINE_LOCATION", "Location")
    )
    CompositionBaseProvider {
        Surface {
            DefaultPermissionsRequestUI(
                permanentlyDenied = permissions,
                needingRationale = emptyList(),
                onRequestPermissions = {},
                onOpenSettings = {}
            )
        }
    }
}
