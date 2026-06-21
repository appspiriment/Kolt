// Moved to compose-kmp. This file is a backward-compatibility wrapper.
// Please migrate imports to: io.github.appspiriment.kolt.composekmp.components.core.messages.AppsBottomSheet
@file:Suppress("unused")

package io.github.appspiriment.kolt.composeutils.components.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

/**
 * @deprecated Moved to compose-kmp. Use [io.github.appspiriment.kolt.composekmp.components.core.messages.AppsBottomSheet].
 */
@Deprecated(
    message = "AppsBottomSheet has been moved to compose-kmp. Use io.github.appspiriment.kolt.composekmp.components.core.messages.AppsBottomSheet",
    replaceWith = ReplaceWith(
        "AppsBottomSheet(showSheet, state, title, dismissSheet, containerColor, showCloseButton, showDragHandle, shape, contentAlignment, contentArrangement, titleAlignment, titlePadding, contentPadding, modifier, content)",
        "io.github.appspiriment.kolt.composekmp.components.core.messages.AppsBottomSheet"
    ),
    level = DeprecationLevel.WARNING
)
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
    io.github.appspiriment.kolt.composekmp.components.core.messages.AppsBottomSheet(
        showSheet = showSheet,
        state = state,
        title = title,
        dismissSheet = dismissSheet,
        containerColor = containerColor,
        showCloseButton = showCloseButton,
        showDragHandle = showDragHandle,
        shape = shape,
        contentAlignment = contentAlignment,
        contentArrangement = contentArrangement,
        titleAlignment = titleAlignment,
        titlePadding = titlePadding,
        contentPadding = contentPadding,
        modifier = modifier,
        content = content,
    )
}