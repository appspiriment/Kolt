package io.github.appspiriment.kolt.updateutils

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.appspiriment.kolt.composeutils.components.containers.AppBarTitle
import io.github.appspiriment.kolt.composeutils.components.containers.AppsPageScaffold
import io.github.appspiriment.kolt.composeutils.components.containers.NavigationMode
import io.github.appspiriment.kolt.composekmp.components.core.buttons.TextButton
import io.github.appspiriment.kolt.composekmp.components.core.buttons.types.ButtonStyle
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composeutils.components.messages.MessageDialog
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.bold
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText
import io.github.appspiriment.kolt.composekmp.wrappers.uiImageResouce
import io.github.appspiriment.kolt.composekmp.wrappers.uiTextResource
import io.github.appspiriment.kolt.utils.extensions.launchPlayStorePage
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

const val CRITICAL_UPDATE = "criticalUpdate"
const val FEATURE_DROP = "featureDrop"

/**
 * Interface for checking and handling app updates.
 *
 * This interface defines the contract for checking for app updates and displaying
 * appropriate UI based on the update type (immediate or flexible).
 *
 * Inherit in the activity with
 * class MainActivity : ComponentActivity(), AppUpdateHelperUtil by AppUpdateHelperUtilImpl()
 */
interface AppUpdateHelperUtil {
    /**
     * Checks for app updates and sets the content based on the update type.
     *
     * This function fetches update information from Firebase Remote Config and
     * determines whether an immediate or flexible update is required. It then
     * displays the appropriate UI (either an immediate update screen or a
     * flexible update dialog) or proceeds to the main content if no update is
     * needed.
     *
     * @param appVersion The current version of the app.
     * @param appName The string resource ID for the app's name.
     * @param brandLogoId The drawable resource ID for the brand logo.
     * @param logoResId The drawable resource ID for the app's logo.
     * @param immediateUpdateMessage The string resource ID for the message to
     *   display for immediate updates.
     * @param flexibleUpdateMessage The string resource ID for the message to
     *   display for flexible updates.
     * @param onCompleted A composable lambda that represents the main content
     *   to display after the update check is completed (or if no update is
     *   needed).
     *
     * @sample
     * AppTheme(dynamicColor = true) {
     *    CheckForUpdateAndSetContent(
     *        appName = R.string.app_name,
     *        .....
     *        onCompleted = {
     *            MainNavGraph()
     *        }
     *    )
     *}
     */
    @Composable
    fun CheckForUpdateAndSetContent(
        appVersion: Int,
        @StringRes appName: Int,
        @DrawableRes brandLogoId: Int,
        @DrawableRes logoResId: Int,
        @StringRes immediateUpdateMessage: Int,
        @StringRes flexibleUpdateMessage: Int,
        onCompleted: @Composable () -> Unit,
    )
}

class AppUpdateHelperUtilImpl : AppUpdateHelperUtil {

    enum class UpdateType {
        /**
         * Represents an immediate update, which requires the user to update the app
         * before continuing.
         */
        IMMEDIATE,

        /**
         * Represents a flexible update, which allows the user to continue using the
         * app but encourages them to update.
         */
        FLEXIBLE,

        /**
         * Represents no update being available or required.
         */
        NONE
    }

    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    private val configSettings = remoteConfigSettings {}

    init {
        remoteConfig.apply {
            setConfigSettingsAsync(configSettings)
        }
    }

    /**
     * Implementation of [AppUpdateHelperUtil] that uses Firebase Remote Config to
     * check for app updates.
     */
    @Composable
    override fun CheckForUpdateAndSetContent(
        appVersion: Int,
        @StringRes appName: Int,
        @DrawableRes brandLogoId: Int,
        @DrawableRes logoResId: Int,
        @StringRes immediateUpdateMessage: Int,
        @StringRes flexibleUpdateMessage: Int,
        onCompleted: @Composable () -> Unit,
    ) {
        val activity = LocalContext.current as Activity
        val updateType = remember { mutableStateOf<UpdateType?>(null) }
        when (updateType.value) {
            UpdateType.IMMEDIATE -> ImmediateUpdateScreen(
                appName = appName,
                logoResId = logoResId,
                brandLogoId = brandLogoId,
                updateMessageId = immediateUpdateMessage
            )
            UpdateType.FLEXIBLE -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    onCompleted()
                    FlexibleUpdateAppSheet(
                        appName = appName,
                        logoResId = logoResId,
                        updateMessageId = flexibleUpdateMessage
                    )
                }
            }
            UpdateType.NONE -> onCompleted()
            null -> {}
        }

        remoteConfig.run {
            fetchAndActivate()
                .addOnCompleteListener(activity) { task ->
                    if (task.isSuccessful) {
                        if (getLong(CRITICAL_UPDATE) > appVersion) {
                            updateType.value = UpdateType.IMMEDIATE
                        } else if (getLong(FEATURE_DROP) > appVersion) {
                            updateType.value = UpdateType.FLEXIBLE
                        } else updateType.value = UpdateType.NONE

                        remoteConfig.reset()
                    } else {
                        updateType.value = UpdateType.NONE
                    }
                }
        }
    }

    @Composable
    fun FlexibleUpdateAppSheet(
        @StringRes appName: Int,
        @DrawableRes logoResId: Int,
        @StringRes updateMessageId: Int
    ) {
        val showUpdateDialog = remember { mutableStateOf(true) }
        val dismissDialog = { showUpdateDialog.value = false }
        val context = LocalContext.current
        if(showUpdateDialog.value){
        MessageDialog(
            title = "Update ${stringResource(appName)}?".toUiText(),
            onDismissRequest = {
                showUpdateDialog.value = false
            },
        ) {

            Image(
                painter = painterResource(logoResId),
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .size(96.dp),
                contentDescription = ""
            )

            AppspirimentText(
                text = uiTextResource(updateMessageId),
                lineHeight = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .padding(end = 16.dp, bottom = 20.dp)
                    .fillMaxWidth()
            ) {
                TextButton(
                    text = "NO THANKS".toUiText(),
                    buttonStyle = ButtonStyle.primaryNegative()
                ) {
                    dismissDialog()
                }

                TextButton(
                    text = "UPDATE".toUiText(),
                    buttonStyle = ButtonStyle.primaryPositive()
                        .copy(buttonColor = Kolt.colors.accentedBlueText)
                ) {
                    dismissDialog()
                }
            }
            HorizontalDivider()
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_logo_get_it_on_google_play),
                    contentDescription = null,
                    modifier = Modifier.clickable { context.launchPlayStorePage() }
                )
            }
        }
        }
    }

    @Composable
    fun ImmediateUpdateScreen(
        @StringRes appName: Int,
        @DrawableRes logoResId: Int,
        @DrawableRes brandLogoId: Int?,
        @StringRes updateMessageId: Int,
    ) {
        val activity = LocalActivity.current as Activity
        AppsPageScaffold(
            appBarTitle = brandLogoId?.let{
                AppBarTitle.BrandLogo(image = uiImageResouce(logoResId))
            } ?: AppBarTitle.ScreenTitleWithIcon(icon = uiImageResouce(logoResId), title = uiTextResource(appName)),
            navMode = NavigationMode.CLOSE,
            navIconClick = {activity.finish()},
            actions = emptyList()
        ) {

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp
                ), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppspirimentText(
                    text = "Update ${stringResource(appName)}".toUiText(),
                    style = Kolt.typography.textLarge.bold
                )

                Image(
                    painter = painterResource(logoResId),
                    modifier = Modifier
                        .padding(top = 48.dp, bottom = 24.dp)
                        .size(128.dp),
                    contentDescription = ""
                )

                AppspirimentText(
                    text = uiTextResource(updateMessageId),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                )
                TextButton(
                    text = "UPDATE".toUiText(),
                    modifier = Modifier.padding(top=16.dp),
                    buttonStyle = ButtonStyle.primaryPositive().copy(buttonColor = Kolt.colors.accentedBlueTitle)
                ) {
                    activity.launchPlayStorePage()
                }
                Spacer(Modifier.weight(1f))

                HorizontalDivider()
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)) {
                    Image(
                        painter = painterResource(R.drawable.ic_logo_get_it_on_google_play),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { activity.launchPlayStorePage() }
                    )
                }
            }
        }
    }
}
