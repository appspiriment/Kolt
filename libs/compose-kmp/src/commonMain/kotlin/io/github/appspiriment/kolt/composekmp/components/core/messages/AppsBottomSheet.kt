package io.github.appspiriment.kolt.composekmp.components.core.messages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.HorizontalSpacer
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.toUiImage

/**
 * A themed modal bottom sheet with optional title, close button, and drag handle.
 *
 * Uses Compose Multiplatform's [ModalBottomSheet] so it works identically on Android,
 * iOS (via CMP), and Desktop. Any platform-specific sheet state must be provided by the
 * caller via [state] (e.g. `rememberModalBottomSheetState()` on Android).
 *
 * @param showSheet          Controls visibility — renders nothing when `false`.
 * @param state              [SheetState] from the caller (typically `rememberModalBottomSheetState()`).
 * @param title              Optional title displayed in the sheet header.
 * @param dismissSheet       Called when the sheet should close (back press, scrim tap, close button).
 * @param containerColor     Background colour of the sheet surface.
 * @param showCloseButton    When `true`, an × icon is shown at the trailing end of the title row.
 * @param showDragHandle     When `true`, the M3 drag handle pill is shown above the title row.
 * @param shape              Shape of the sheet container.
 * @param contentAlignment   Horizontal alignment inside the content [Column].
 * @param contentArrangement Vertical arrangement inside the content [Column].
 * @param titleAlignment     Horizontal arrangement of the title text (when no close button).
 * @param titlePadding       Padding applied to the title/close row.
 * @param contentPadding     Padding applied to the content [Column].
 * @param modifier           Applied to the [ModalBottomSheet].
 * @param content            Sheet body content, rendered inside a [ColumnScope].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsBottomSheet(
    showSheet: Boolean,
    state: SheetState,
    title: UiText? = null,
    dismissSheet: () -> Unit,
    containerColor: Color = Kolt.colors.background,
    showCloseButton: Boolean = true,
    showDragHandle: Boolean = true,
    shape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    contentArrangement: Arrangement.Vertical = Arrangement.Top,
    titleAlignment: Arrangement.Horizontal = Arrangement.Center,
    titlePadding: PaddingValues = PaddingValues(16.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit)
) {
    if (showSheet) {
        ModalBottomSheet(
            modifier = modifier,
            onDismissRequest = dismissSheet,
            sheetState = state,
            shape = shape,
            containerColor = containerColor,
            dragHandle = if (showDragHandle) { { BottomSheetDefaults.DragHandle() } } else null
        ) {
            BottomSheetContent(
                title = title,
                dismissSheet = dismissSheet,
                showCloseButton = showCloseButton,
                contentAlignment = contentAlignment,
                contentArrangement = contentArrangement,
                titlePadding = titlePadding,
                titleAlignment = titleAlignment,
                contentPadding = contentPadding,
                content = content,
            )
        }
    }
}

@Composable
private fun BottomSheetContent(
    title: UiText? = null,
    dismissSheet: () -> Unit,
    showCloseButton: Boolean = true,
    contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    titleAlignment: Arrangement.Horizontal = Arrangement.Center,
    contentArrangement: Arrangement.Vertical = Arrangement.Top,
    titlePadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    content: @Composable() (ColumnScope.() -> Unit)
) {
    if (showCloseButton || title != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(titlePadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (showCloseButton) Arrangement.SpaceBetween else titleAlignment
        ) {
            if (showCloseButton) {
                HorizontalSpacer(Kolt.sizes.paddingXLarge)
            }
            title?.let {
                AppspirimentText(
                    text = it,
                    style = Kolt.typography.textLarge.semiBold
                )
            }
            if (showCloseButton) {
                AppsIcon(
                    icon = Icons.Default.Close.toUiImage(),
                    modifier = Modifier
                        .clickable { dismissSheet() }
                        .size(Kolt.sizes.iconStandardLarge)
                )
            }
        }
    } else {
        Spacer(modifier = Modifier.padding(titlePadding))
    }

    Column(
        horizontalAlignment = contentAlignment,
        verticalArrangement = contentArrangement,
        modifier = Modifier.padding(contentPadding)
    ) {
        content()
    }
}
