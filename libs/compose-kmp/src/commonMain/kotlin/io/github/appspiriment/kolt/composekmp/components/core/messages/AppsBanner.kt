package io.github.appspiriment.kolt.composekmp.components.core.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.buttons.AppsTextButton
import io.github.appspiriment.kolt.composekmp.components.core.buttons.types.ButtonStyle
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.toUiImage

// ── Style enum ─────────────────────────────────────────────────────────────────

/** Semantic intent for [AppsBanner]. Controls default colours and leading icon. */
enum class BannerStyle { Info, Success, Warning, Error, Neutral }

// ── Colours ───────────────────────────────────────────────────────────────────

/**
 * Resolved colour pair for [AppsBanner].
 *
 * Obtain from [appsBannerColors] for a theme-driven pair, or construct directly
 * for fully custom colours.
 */
@Immutable
data class BannerColors(
    val containerColor: Color,
    val contentColor: Color,
)

/** Returns the [BannerColors] for [style] from the active theme. */
@Composable
fun appsBannerColors(style: BannerStyle): BannerColors = when (style) {
    BannerStyle.Info    -> BannerColors(Kolt.colors.infoContainer,    Kolt.colors.onInfoContainer)
    BannerStyle.Success -> BannerColors(Kolt.colors.successContainer, Kolt.colors.onSuccessContainer)
    BannerStyle.Warning -> BannerColors(Kolt.colors.warningContainer, Kolt.colors.onWarningContainer)
    BannerStyle.Error   -> BannerColors(Kolt.colors.errorContainer,   Kolt.colors.onErrorContainer)
    BannerStyle.Neutral -> BannerColors(Kolt.colors.greyCardContainer, Kolt.colors.onGreyCardContainer)
}

// ── Error icon (inline path — avoids kotlin.Error name collision) ─────────────

/** Filled exclamation-circle, semantically equivalent to Icons.Default.Error. */
private val ErrorBannerIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "ErrorBannerIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            fill = SolidColor(Color.Black),
        ) {
            // Filled circle
            moveTo(12f, 2f)
            curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
            curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
            curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
            curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
            close()
            // Exclamation stem
            moveTo(13f, 17f)
            lineTo(11f, 17f)
            lineTo(11f, 15f)
            lineTo(13f, 15f)
            close()
            // Exclamation dot
            moveTo(13f, 13f)
            lineTo(11f, 13f)
            lineTo(11f, 7f)
            lineTo(13f, 7f)
            close()
        }
    }.build()
}

// ── Default icon per style ────────────────────────────────────────────────────

private fun defaultBannerIcon(style: BannerStyle): UiImage? = when (style) {
    BannerStyle.Info    -> Icons.Default.Info.toUiImage()
    BannerStyle.Success -> Icons.Default.CheckCircle.toUiImage()
    BannerStyle.Warning -> Icons.Default.Warning.toUiImage()
    BannerStyle.Error   -> ErrorBannerIcon.toUiImage()
    BannerStyle.Neutral -> null
}

// ── Component ─────────────────────────────────────────────────────────────────

/**
 * An inline themed alert banner for surface-level feedback.
 *
 * Unlike a Snackbar (ephemeral floating overlay), [AppsBanner] is rendered inline in the
 * layout — suitable for persistent session warnings, informational notices, or content-level
 * alerts that the user should not be able to miss.
 *
 * Each [BannerStyle] resolves a container/content colour pair from the active theme and
 * provides a default leading icon. Override either via [colors] or [leadingIcon].
 *
 * ```kotlin
 * AppsBanner(
 *     message = "Your subscription expires in 3 days.".toUiText(),
 *     style = BannerStyle.Warning,
 *     actionText = "Renew now".toUiText(),
 *     onAction = { navController.navigate(Route.Subscription) },
 * )
 * ```
 *
 * @param message       Primary message text.
 * @param modifier      Applied to the outer container [Row].
 * @param style         Semantic intent — drives container/content colour and default icon.
 * @param title         Optional bold title displayed above [message].
 * @param leadingIcon   Leading icon. Defaults to the style's icon; pass `null` to suppress.
 * @param actionText    Optional inline action label rendered as a ghost button below [message].
 * @param onAction      Called when [actionText] is tapped.
 * @param dismissible   When `true`, a close [IconButton] is appended at the trailing end.
 * @param onDismiss     Called when the close button is tapped.
 * @param colors        Colour override. Defaults to [appsBannerColors] for [style].
 * @param shape         Shape of the banner surface.
 * @param iconSize      Size of [leadingIcon] and the dismiss icon. Defaults to `iconSmall`.
 * @param contentPadding Padding inside the banner row around the icon, text column, and dismiss
 *                      button. Defaults to `paddingSmallMedium` horizontal × `paddingSmall`
 *                      vertical, matching the standard compact-banner spec.
 * @param titleStyle    Text style for [title].
 * @param messageStyle  Text style for [message].
 */
@Composable
fun AppsBanner(
    message: UiText,
    modifier: Modifier = Modifier,
    style: BannerStyle = BannerStyle.Info,
    title: UiText? = null,
    leadingIcon: UiImage? = defaultBannerIcon(style),
    actionText: UiText? = null,
    onAction: () -> Unit = {},
    dismissible: Boolean = false,
    onDismiss: () -> Unit = {},
    colors: BannerColors = appsBannerColors(style),
    shape: Shape = RoundedCornerShape(Kolt.sizes.cornerRadiusMedium),
    iconSize: Dp = Kolt.sizes.iconSmall,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = Kolt.sizes.paddingSmallMedium,
        vertical = Kolt.sizes.paddingSmall,
    ),
    titleStyle: TextStyle = Kolt.typography.bodySmall.semiBold,
    messageStyle: TextStyle = Kolt.typography.bodySmall,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.containerColor, shape)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Kolt.sizes.paddingSmall),
    ) {
        leadingIcon?.let { icon ->
            AppsIcon(
                icon = icon,
                modifier = Modifier.size(iconSize),
                tint = colors.contentColor,
            )
        }

        // Text + optional action — expands to fill remaining width
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Kolt.sizes.paddingXXSmall),
        ) {
            title?.let {
                AppspirimentText(text = it, style = titleStyle, color = colors.contentColor)
            }
            AppspirimentText(text = message, style = messageStyle, color = colors.contentColor)
            actionText?.let {
                AppsTextButton(
                    text = it,
                    buttonStyle = ButtonStyle.transparent(
                        textColor = colors.contentColor,
                        buttonPressedColor = colors.contentColor.copy(alpha = 0.08f),
                        disabledContentColor = colors.contentColor.copy(alpha = 0.38f),
                    ),
                    contentPadding = PaddingValues(
                        start = Kolt.sizes.noPadding,
                        top = Kolt.sizes.noPadding,
                        end = Kolt.sizes.paddingSmall,
                        bottom = Kolt.sizes.noPadding,
                    ),
                    onClick = onAction,
                )
            }
        }

        // Dismiss button — "Dismiss" used as contentDescription (no Android R dependency)
        if (dismissible) {
            IconButton(onClick = onDismiss) {
                AppsIcon(
                    icon = Icons.Default.Close.toUiImage(),
                    modifier = Modifier.size(iconSize),
                    tint = colors.contentColor,
                    contentDescription = "Dismiss",
                )
            }
        }
    }
}
