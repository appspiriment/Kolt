package io.github.appspiriment.kolt.composekmp.components.core.selectors


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.toUiImage


/**
 * Sealed class representing the background color and shape for a selectable item.
 */
sealed class SelectableBackground(
    open val color: Color,
    open val shape: Shape,
){
    data class Selected(override val color: Color, override val shape:Shape) : SelectableBackground(
        color = color, shape = shape
    )

    data class Unselected(override val color: Color, override val shape:Shape) : SelectableBackground(
        color = color, shape = shape
    )
}

/**
 * Sealed class representing the text content and its styling based on selection state.
 */
sealed class TextState(
    open val textColor: Color,
    open val textStyle: TextStyle,
    open val isHtml: Boolean = false,
    open val letterSpacing: TextUnit = TextUnit.Unspecified,
    open val textDecoration: TextDecoration? = null,
    open val textAlign: TextAlign? = null,
    open val lineHeight: TextUnit = TextUnit.Unspecified,
    open val overflow: TextOverflow = TextOverflow.Clip,
    open val softWrap: Boolean = true,
    open val maxLines: Int = Int.MAX_VALUE,
    open val onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    data class Selected(
        override val textColor: Color,
        override val textStyle: TextStyle,
        override val isHtml: Boolean = false,
        override val letterSpacing: TextUnit = TextUnit.Unspecified,
        override val textDecoration: TextDecoration? = null,
        override val textAlign: TextAlign? = null,
        override val lineHeight: TextUnit = TextUnit.Unspecified,
        override val overflow: TextOverflow = TextOverflow.Clip,
        override val softWrap: Boolean = true,
        override val maxLines: Int = Int.MAX_VALUE,
        override val onTextLayout: (TextLayoutResult) -> Unit = {},
    ) : TextState(textColor = textColor, textStyle = textStyle, isHtml = isHtml,
        letterSpacing = letterSpacing, textDecoration = textDecoration, textAlign = textAlign,
        lineHeight = lineHeight, overflow = overflow, softWrap = softWrap, maxLines = maxLines,
        onTextLayout = onTextLayout
    )

    data class Unselected(
        override val textColor: Color,
        override val textStyle: TextStyle,
        override val isHtml: Boolean = false,
        override val letterSpacing: TextUnit = TextUnit.Unspecified,
        override val textDecoration: TextDecoration? = null,
        override val textAlign: TextAlign? = null,
        override val lineHeight: TextUnit = TextUnit.Unspecified,
        override val overflow: TextOverflow = TextOverflow.Clip,
        override val softWrap: Boolean = true,
        override val maxLines: Int = Int.MAX_VALUE,
        override val onTextLayout: (TextLayoutResult) -> Unit = {},
    ) : TextState(
        textColor = textColor, textStyle = textStyle, isHtml = isHtml,
        letterSpacing = letterSpacing, textDecoration = textDecoration, textAlign = textAlign,
        lineHeight = lineHeight, overflow = overflow, softWrap = softWrap, maxLines = maxLines,
        onTextLayout = onTextLayout
    )
}

/**
 * Sealed class representing the icon, its size, and a modifier based on selection state.
 */
sealed class IconState(
    open val icon: UiImage,
    open val iconHeight: Dp = Dp.Unspecified,
    open val iconModifier: Modifier = Modifier
) {
    data class Selected(
        override val icon: UiImage,
        override val iconHeight: Dp = Dp.Unspecified,
        override val iconModifier: Modifier = Modifier
    ) : IconState(icon = icon, iconHeight = iconHeight, iconModifier = iconModifier)

    data class Unselected(
        override val icon: UiImage,
        override val iconHeight: Dp = Dp.Unspecified,
        override val iconModifier: Modifier = Modifier
    ) : IconState(icon = icon, iconHeight = iconHeight, iconModifier = iconModifier)
}

/**
 * Sealed class representing the complete state of a selectable item.
 * This encapsulates text, background, and icon states for both selected and unselected conditions.
 */
sealed class SelectableState(
    open val textState: TextState,
    open val background: SelectableBackground,
    open val iconState: IconState?,
    open val horizontalArrangement: Arrangement.Horizontal
) {
    data class Selected(
        override val textState: TextState.Selected,
        override val background: SelectableBackground.Selected,
        override val iconState: IconState.Selected? = null,
        override val horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceAround
    ) : SelectableState(
        textState = textState, background = background, iconState = iconState, horizontalArrangement = horizontalArrangement
    )

    data class Unselected(
        override val textState: TextState.Unselected,
        override val background: SelectableBackground.Unselected,
        override val iconState: IconState.Unselected? = null,
        override val horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween
    ) : SelectableState(
        textState = textState, background = background, iconState = iconState, horizontalArrangement = horizontalArrangement
    )
}

/**
 * Provides default configurations for [AppsSelectableText] components.
 */
object AppsSelectableDefaults{

    @Composable fun selectedText(
        textColor: Color = Kolt.colors.onMainSurface,
        textStyle: TextStyle = Kolt.typography.textMedium,
        isHtml: Boolean = false,
        letterSpacing: TextUnit = TextUnit.Unspecified,
        textDecoration: TextDecoration? = null,
        textAlign: TextAlign? = null,
        lineHeight: TextUnit = TextUnit.Unspecified,
        overflow: TextOverflow = TextOverflow.Clip,
        softWrap: Boolean = true,
        maxLines: Int = Int.MAX_VALUE,
        onTextLayout: (TextLayoutResult) -> Unit = {},
    ) = TextState.Selected(
        textColor = textColor,
        textStyle = textStyle,
        isHtml = isHtml,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        onTextLayout = onTextLayout
    )

    @Composable fun unselectedText(
        textColor: Color = Kolt.colors.onMainSurface,
        textStyle: TextStyle = Kolt.typography.textMedium,
        isHtml: Boolean = false,
        letterSpacing: TextUnit = TextUnit.Unspecified,
        textDecoration: TextDecoration? = null,
        textAlign: TextAlign? = null,
        lineHeight: TextUnit = TextUnit.Unspecified,
        overflow: TextOverflow = TextOverflow.Clip,
        softWrap: Boolean = true,
        maxLines: Int = Int.MAX_VALUE,
        onTextLayout: (TextLayoutResult) -> Unit = {},
    ) = TextState.Unselected(
        textColor = textColor,
        textStyle = textStyle,
        isHtml = isHtml,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        onTextLayout = onTextLayout
    )

    @Composable fun selectedBackground(
        color: Color = MaterialTheme.colorScheme.primaryContainer,
        shape: Shape = RoundedCornerShape(Kolt.sizes.cornerRadiusMedium)
    ) = SelectableBackground.Selected(
        color = color,
        shape = shape
    )

    @Composable fun unselectedBackground(
        color: Color = Color.Transparent,
        shape: Shape = RoundedCornerShape(Kolt.sizes.cornerRadiusMedium)
    ) = SelectableBackground.Unselected(
        color = color,
        shape = shape
    )

    @Composable
    fun selectedIcon(
        icon: UiImage,
        iconHeight: Dp = Kolt.sizes.iconStandard,
        iconModifier: Modifier = Modifier
    ) = IconState.Selected(icon = icon, iconHeight = iconHeight, iconModifier = iconModifier)

    @Composable
    fun unselectedIcon(
        icon: UiImage,
        iconHeight: Dp = Kolt.sizes.iconStandard,
        iconModifier: Modifier = Modifier
    ) = IconState.Unselected(icon = icon, iconHeight = iconHeight, iconModifier = iconModifier)

    @Composable
    fun selectedState(
        textState: TextState.Selected,
        background: SelectableBackground.Selected,
        iconState: IconState.Selected? = null,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween
    ) = SelectableState.Selected(
        textState = textState,
        background = background,
        iconState = iconState,
        horizontalArrangement = horizontalArrangement
    )

    @Composable
    fun unselectedState(
        textState: TextState.Unselected,
        background: SelectableBackground.Unselected,
        iconState: IconState.Unselected? = null,
        horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceAround
    ) = SelectableState.Unselected(
        textState = textState,
        background = background,
        iconState = iconState,
        horizontalArrangement = horizontalArrangement
    )
}

/**
 * A Jetpack Compose component for selectable text with configurable background color,
 * and text/icon content that changes based on selection state.
 *
 * When the item is clicked, its selection state toggles.
 * If selected, the background color changes to [selectedBackgroundColor].
 * The text and icon displayed are determined by the provided [unselectedTextState],
 * [selectedTextState], [unselectedIconState], and [selectedIconState].
 *
 * @param unselectedState The [SelectableState.Unselected] defining the text, background, and icon when unselected.
 * @param selectedState The [SelectableState.Selected] defining the text, background, and icon when selected.
 * @param iconPadding The spacing between the text and the icon.
 * @param modifier The modifier to be applied to the root row of the component.
 * @param initialSelectedState The initial selection state of the component. Defaults to false.
 * @param onTextSelected A callback that is invoked when the selection state changes.
 * Provides the new selection state as a boolean.
 */
@Composable
fun AppsSelectableText(
    text: UiText,
    unselectedState: SelectableState.Unselected,
    selectedState: SelectableState.Selected,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    initialSelectedState: Boolean = false,
    onTextSelected: (Boolean) -> Unit = {},
) {
    // State to manage the selection status of the item.
    var isSelected by remember { mutableStateOf(initialSelectedState) }

    // Determine current text and icon based on selection state
    val currentSelectableState = if (isSelected) selectedState else unselectedState
    val currentTextState = currentSelectableState.textState
    val currentIconState = currentSelectableState.iconState
    val currentBackgroundState = currentSelectableState.background

    Row(
        modifier = modifier
            .fillMaxWidth() // Make the row fill the width available to it
            .clickable {
                // Toggle the selection state when the row is clicked.
                isSelected = !isSelected
                // Invoke the callback with the new selection state.
                onTextSelected(isSelected)
            }
            .background(
                color =currentBackgroundState.color,
                shape = currentBackgroundState.shape
            )
            .padding(Kolt.sizes.paddingMedium), // Apply padding here for the whole row
        verticalAlignment = Alignment.CenterVertically, // Vertically center items in the row
        horizontalArrangement = currentSelectableState.horizontalArrangement // Space out content and icon
    ) {
        // Display the main text content using AppspirimentText.
        AppspirimentText(
            text = text,
            style = currentTextState.textStyle,
            color = currentTextState.textColor,
            letterSpacing = currentTextState.letterSpacing,
            textDecoration = currentTextState.textDecoration,
            textAlign = currentTextState.textAlign,
            lineHeight = currentTextState.lineHeight,
            overflow = currentTextState.overflow,
            softWrap = currentTextState.softWrap,
            maxLines = currentTextState.maxLines,
            isHtml = currentTextState.isHtml,
            modifier = textModifier.offset(y=1.dp),
            onTextLayout = currentTextState.onTextLayout
        )

        // Conditionally display the icon if available for the current state.
        currentIconState?.let {
            AppsIcon(
                icon = it.icon,
                iconHeight = it.iconHeight,
                modifier = it.iconModifier.padding(start = Kolt.sizes.paddingXXSmall)
            )
        }
    }
}

/**
 * Preview Composable for the AppsSelectableText component.
 * Shows examples of how the component looks in different states.
 */
@Composable
fun AppsSelectableTextPreview() {
    MaterialTheme { // Wrap in MaterialTheme for proper styling
        Surface(color = MaterialTheme.colorScheme.background) {
            // Example 1: Text and Icon change on selection, with iconSize
            AppsSelectableText(
                unselectedState = AppsSelectableDefaults.unselectedState(
                    textState = AppsSelectableDefaults.unselectedText(),
                    background = AppsSelectableDefaults.unselectedBackground(),
                    iconState = AppsSelectableDefaults.unselectedIcon(icon = Icons.Default.Contactless.toUiImage(), iconHeight = 20.dp)
                ),
                selectedState = AppsSelectableDefaults.selectedState(
                    textState = AppsSelectableDefaults.selectedText(),
                    background = AppsSelectableDefaults.selectedBackground(color = MaterialTheme.colorScheme.primaryContainer),
                    iconState = AppsSelectableDefaults.selectedIcon(icon = Icons.Default.Contactless.toUiImage(),  iconHeight = 24.dp)
                ),
                text = UiText.DynamicString("Selected!"),
                onTextSelected = { selected ->
                    println("Item 1 selected: $selected")
                }
            )

            // Example 2: Text and color change, no icon when unselected, custom icon when selected with modifier
            AppsSelectableText(
                unselectedState = AppsSelectableDefaults.unselectedState(
                    textState = AppsSelectableDefaults.unselectedText(
                        textColor = Color.DarkGray // Updated to use extension function
                    ),
                    background = AppsSelectableDefaults.unselectedBackground(),
                    iconState = null // No icon when unselected
                ),
                selectedState = AppsSelectableDefaults.selectedState(
                    textState = AppsSelectableDefaults.selectedText(
                        textColor = MaterialTheme.colorScheme.primary // Updated to use extension function
                    ),
                    background = AppsSelectableDefaults.selectedBackground(color = MaterialTheme.colorScheme.secondaryContainer),
                    iconState = AppsSelectableDefaults.selectedIcon(
                        icon = Icons.Default.Contactless.toUiImage(),
                        iconHeight = 28.dp,
                        iconModifier = Modifier.padding(start = 4.dp)
                    )
                ),
                text = UiText.DynamicString("Now selected, with custom color!"),
                initialSelectedState = false,
                modifier = Modifier.padding(top = 80.dp),
                onTextSelected = { selected ->
                    println("Item 2 selected: $selected")
                }
            )

            // Example 3: Initially selected, text and icon change on unselection, with custom icon size and modifier
            AppsSelectableText(
                unselectedState = AppsSelectableDefaults.unselectedState(
                    textState = AppsSelectableDefaults.unselectedText(
                        textColor = Color.Red // Updated to use extension function
                    ),
                    background = AppsSelectableDefaults.unselectedBackground(color = Color(0xFFD1E7DD)),
                    iconState = AppsSelectableDefaults.unselectedIcon(
                        icon = Icons.Default.Info.toUiImage(), // Updated to use extension function
                        iconHeight = 20.dp,
                        iconModifier = Modifier.padding(end = 2.dp)
                    )
                ),
                selectedState = AppsSelectableDefaults.selectedState(
                    textState = AppsSelectableDefaults.selectedText(
                        textColor = Color.Green // Updated to use extension function
                    ),
                    background = AppsSelectableDefaults.selectedBackground(color = Color(0xFFD1E7DD)),
                    iconState = AppsSelectableDefaults.selectedIcon(
                        icon = Icons.Default.CheckCircle.toUiImage(), // Updated to use extension function
                        iconHeight = 20.dp,
                        iconModifier = Modifier.padding(end = 2.dp)
                    )
                ),
                text = UiText.DynamicString("Initially selected!"),

                initialSelectedState = true,
                modifier = Modifier.padding(top = 160.dp),
                onTextSelected = { selected ->
                    println("Item 3 selected: $selected")
                }
            )

            // Example 4: Only icon changes, text remains constant, with default icon size
            AppsSelectableText(
                unselectedState = AppsSelectableDefaults.unselectedState(
                    textState = AppsSelectableDefaults.unselectedText(),
                    background = AppsSelectableDefaults.unselectedBackground(color = Color(0xFFCCE5FF)),
                    iconState = AppsSelectableDefaults.unselectedIcon(icon = Icons.Default.Info.toUiImage()) // Updated to use extension function
                ),
                selectedState = AppsSelectableDefaults.selectedState(
                    textState = AppsSelectableDefaults.selectedText(),
                    background = AppsSelectableDefaults.selectedBackground(color = Color(0xFFCCE5FF)),
                    iconState = AppsSelectableDefaults.selectedIcon(icon = Icons.Default.CheckCircle.toUiImage()) // Updated to use extension function
                ),
                modifier = Modifier.padding(top = 240.dp),
                text = UiText.DynamicString("Constant text"),
                onTextSelected = { selected ->
                    println("Item 4 selected: $selected")
                }
            )
        }
    }
}