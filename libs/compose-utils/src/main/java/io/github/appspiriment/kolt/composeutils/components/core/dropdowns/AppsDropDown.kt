package io.github.appspiriment.kolt.composeutils.components.core.dropdowns

import io.github.appspiriment.kolt.composekmp.wrappers.asString
import android.R.attr.scaleX
import android.R.attr.scaleY
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.Kolt.colors
import io.github.appspiriment.kolt.composekmp.theme.Kolt.sizes
import io.github.appspiriment.kolt.composekmp.wrappers.UiColor
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.toUiImage
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText

data class DropdownColors(
    val containerColor: Color,
    val dropdownMenuColor: Color,
    val contentColor: Color,
    val border: BorderStroke,
    val shape: Shape = RoundedCornerShape(12.dp)
)

data class DropdownItemStyle(
    val textStyle: TextStyle,
    val color: Color,
    val modifier: Modifier = Modifier
)

object AppsDropdownDefaults {

    @Composable
    fun defaultColors(
        containerColor: Color = Kolt.colors.mainSurface,
        dropdownMenuColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor: Color = Kolt.colors.onMainSurface,
        borderColor: Color = Kolt.colors.dividerColor,
        borderWidth: Dp = 1.dp,
        shape: Shape = RoundedCornerShape(12.dp)
    ) = DropdownColors(
        containerColor = containerColor,
        dropdownMenuColor = dropdownMenuColor,
        contentColor = contentColor,
        border = BorderStroke(borderWidth, borderColor),
        shape = shape
    )

    @Composable
    fun errorColors(
        containerColor: Color = MaterialTheme.colorScheme.surface,
        dropdownMenuColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor: Color = MaterialTheme.colorScheme.error,
        borderColor: Color = MaterialTheme.colorScheme.error,
        borderWidth: Dp = 1.5.dp,
        shape: Shape = RoundedCornerShape(12.dp)
    ) = DropdownColors(
        containerColor = containerColor,
        dropdownMenuColor = dropdownMenuColor,
        contentColor = contentColor,
        border = BorderStroke(borderWidth, borderColor),
        shape = shape
    )

    @Composable
    fun defaultLabelStyle() = DropdownItemStyle(
        textStyle = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
    )

    @Composable
    fun defaultPlaceholderStyle() = DropdownItemStyle(
        textStyle = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )

    @Composable
    fun defaultItemBaseStyle() = DropdownItemStyle(
        textStyle = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun DropdownLabel(
    text: UiText,
    style: DropdownItemStyle = AppsDropdownDefaults.defaultLabelStyle()
) {
    AppspirimentText(
        text = text,
        style = style.textStyle,
        color = style.color,
        modifier = style.modifier
    )
}

@Composable
fun DropdownPlaceholder(
    text: UiText,
    style: DropdownItemStyle = AppsDropdownDefaults.defaultPlaceholderStyle()
) {
    AppspirimentText(
        text = text,
        style = style.textStyle,
        color = style.color,
        modifier = style.modifier
    )
}

@Composable
fun DropdownChevron(
    tint: UiColor = Kolt.uiColors.iconTint
) {
    AppsIcon(
        icon = Icons.Default.ArrowDropDown.toUiImage(tint = tint),
        modifier = Modifier.size(sizes.iconStandard)
    )
}

@Composable
private fun rememberEffectiveItemStyle(
    override: ((Boolean) -> DropdownItemStyle)? = null
): (Boolean) -> DropdownItemStyle {
    val base = AppsDropdownDefaults.defaultItemBaseStyle()
    val primary = Kolt.colors.primary

    return override ?: { isSelected ->
        if (isSelected) {
            base.copy(
                color = primary,
                textStyle = base.textStyle.copy(fontWeight = FontWeight.SemiBold)
            )
        } else {
            base
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppsDropdownCore(
    options: List<Any?>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    label: @Composable (() -> Unit)?,
    placeholder: @Composable (() -> Unit)?,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    itemContent: @Composable (Any?, Boolean) -> Unit,
    colors: DropdownColors,
    itemStyle: ((Boolean) -> DropdownItemStyle)?,
    matchFieldWidth: Boolean,
    maxMenuHeight: Dp,
    selectedText: UiText
) {
    val effectiveItemStyle = rememberEffectiveItemStyle(itemStyle)
    var expanded by remember { mutableStateOf(false) }
    val hasValue = selectedText.asString().isNotEmpty()

    // The label is "Active" (moves up) if the menu is open OR if a value is picked
    val isElevated = expanded || hasValue

    // Animations for the floating effect
    val labelOffset by animateDpAsState(
        targetValue = if (isElevated) (-40).dp else 0.dp, // -30dp gives room for Malayalam glyphs
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "LabelOffset"
    )
    val labelScale by animateFloatAsState(
        targetValue = if (isElevated) 0.95f else 1f,
        label = "LabelScale"
    )
    val labelColor by animateColorAsState(
        targetValue = if (expanded) Kolt.colors.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "LabelColor"
    )

    val density = LocalDensity.current
    var containerWidthPx by remember { mutableIntStateOf(0) }

    // Column provides top padding so the elevated label isn't clipped
    Column(modifier = modifier.padding(top = 24.dp)) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {

            // 1. The Main Dropdown Field (Bottom Layer)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { if (enabled) expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedText.asString(),
                    onValueChange = {},
                    readOnly = true,
                    enabled = enabled,
                    label = null, // IMPORTANT: Standard label is null to avoid the white mask "eyesore"

                    // FIXED: Placeholder only appears when the Label has moved to the top
                    placeholder = if (isElevated && !hasValue) placeholder else null,

                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colors.containerColor,
                        unfocusedContainerColor = colors.containerColor,
                        disabledContainerColor = colors.containerColor,
                        focusedIndicatorColor = Kolt.colors.primary,
                        unfocusedIndicatorColor = Kolt.colors.dividerColor,
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    ),
                    shape = colors.shape,
                    modifier = Modifier
                        .menuAnchor()
                        .onGloballyPositioned { coordinates ->
                            containerWidthPx = coordinates.size.width
                        }
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .width(
                            if (matchFieldWidth) with(density) { containerWidthPx.toDp() }
                            else Dp.Unspecified.coerceAtLeast(180.dp)
                        )
                        .heightIn(max = maxMenuHeight)
                        .background(colors.dropdownMenuColor)
                ) {
                    options.forEachIndexed { index, item ->
                        val isItemSelected = index == selectedIndex
                        val currentStyle = effectiveItemStyle(isItemSelected)

                        DropdownMenuItem(
                            text = {
                                Box(modifier = currentStyle.modifier) {
                                    itemContent(item, isItemSelected)
                                }
                            },
                            onClick = {
                                onItemSelected(index)
                                expanded = false
                            },
                            contentPadding = PaddingValues(0.dp)
                        )
                    }
                }
            }

            // 2. The Animated Label (Top Layer)
            if (label != null) {
                Box(
                    modifier = Modifier
                        .offset(
                            y = labelOffset,
                            // Adjust X based on whether we are currently "inside" with an icon
                            x = if (leadingIcon != null && !isElevated) 48.dp else 12.dp
                        )
                        .graphicsLayer {
                            scaleX = labelScale
                            scaleY = labelScale
                            transformOrigin = TransformOrigin(0f, 0.5f)
                        }
                        // Ignore pointer events when elevated so the user can click the field "through" the label
                        .then(if (isElevated) Modifier else Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { if (enabled) expanded = true })
                ) {
                    CompositionLocalProvider(LocalContentColor provides labelColor) {
                        label()
                    }
                }
            }
        }
    }
}

@Composable
fun <T> AppsDropdown(
    options: List<T>,
    selectedIndex: Int = -1,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labelText: UiText? = null,
    selectedItemExtractor: (T) -> UiText,
    placeholderText: UiText? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = { DropdownChevron() },
    colors: DropdownColors = AppsDropdownDefaults.defaultColors(),
    itemStyle: ((Boolean) -> DropdownItemStyle)? = null,
    itemContent: @Composable (T, Boolean) -> Unit = { item, isSelected ->
        val style = rememberEffectiveItemStyle(itemStyle)(isSelected)
        AppspirimentText(
            text = selectedItemExtractor(item),
            style = style.textStyle,
            color = style.color,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = sizes.paddingMedium, vertical = sizes.paddingSmallMedium)
        )
    },
    matchFieldWidth: Boolean = true,
    maxMenuHeight: Dp = 280.dp
) {
    val selectedText = remember(selectedIndex, options) {
        if (selectedIndex in options.indices) {
            selectedItemExtractor(options[selectedIndex])
        } else {
            "".toUiText() // Empty field if nothing selected
        }
    }

    AppsDropdownCore(
        options = options,
        selectedIndex = selectedIndex,
        onItemSelected = onItemSelected,
        modifier = modifier,
        enabled = enabled,
        label = labelText?.let { { DropdownLabel(it) } },
        placeholder = placeholderText?.let { { DropdownPlaceholder(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        itemContent = { item, selected -> itemContent(item as T, selected) },
        colors = colors,
        itemStyle = itemStyle,
        matchFieldWidth = matchFieldWidth,
        maxMenuHeight = maxMenuHeight,
        selectedText = selectedText
    )
}

@Composable
fun AppsDropdown(
    options: List<UiText>,
    selectedIndex: Int = -1,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    labelText: UiText? = null,
    placeholderText: UiText? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = { DropdownChevron() },
    colors: DropdownColors = AppsDropdownDefaults.defaultColors(),
    itemStyle: ((Boolean) -> DropdownItemStyle)? = null,
    matchFieldWidth: Boolean = true,
    maxMenuHeight: Dp = 280.dp
) {
    AppsDropdown(
        options = options,
        selectedIndex = selectedIndex,
        onItemSelected = onItemSelected,
        modifier = modifier,
        enabled = enabled,
        labelText = labelText,
        placeholderText = placeholderText,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        colors = colors,
        itemStyle = itemStyle,
        selectedItemExtractor = {
            options.getOrElse(selectedIndex) { "".toUiText() }
        },
        itemContent = { text, isSelected ->
            val style = rememberEffectiveItemStyle(itemStyle)(isSelected)

            AppspirimentText(
                text = text,
                style = style.textStyle,
                color = style.color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        },
        matchFieldWidth = matchFieldWidth,
        maxMenuHeight = maxMenuHeight
    )
}


//PREIVEW

@Preview(showBackground = true, name = "AppsDropdown - UiText + String labels")
@Composable
private fun AppsDropdownUiTextPreview() {
    var selectedIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Basic usage with UiText options + string labels
        AppsDropdown(
            options = listOf(
                UiText.DynamicString("Karnataka"),
                UiText.DynamicString("Kerala"),
                UiText.DynamicString("Tamil Nadu"),
                UiText.DynamicString("Andhra Pradesh"),
                UiText.DynamicString("Telangana"),
                UiText.DynamicString("Goa"),
                UiText.DynamicString("Maharashtra")
            ),
            selectedIndex = selectedIndex,
            onItemSelected = { selectedIndex = it },
            labelText = "Select State".toUiText(),
            placeholderText = "Choose your state".toUiText(),
            matchFieldWidth = true,
            maxMenuHeight = 240.dp
        )

        // Wide menu example
        AppsDropdown(
            options = listOf(
                UiText.DynamicString("Bengaluru Urban"),
                UiText.DynamicString("Mysuru"),
                UiText.DynamicString("Hubballi-Dharwad"),
                UiText.DynamicString("Mangaluru"),
                UiText.DynamicString("Belagavi"),
                UiText.DynamicString("Kalaburagi"),
                UiText.DynamicString("Ballari"),
                UiText.DynamicString("Vijayapura"),
                UiText.DynamicString("Shivamogga")
            ),
            selectedIndex = selectedIndex,
            onItemSelected = { selectedIndex = it },
            labelText = "Select District".toUiText(),
            placeholderText = "Select district (wide menu)".toUiText(),
            matchFieldWidth = false,
            maxMenuHeight = 320.dp
        )

        // Disabled state
        AppsDropdown(
            options = listOf(
                UiText.DynamicString("Male"),
                UiText.DynamicString("Female"),
                UiText.DynamicString("Other")
            ),
            selectedIndex = 0,
            onItemSelected = {},
            enabled = false,
            labelText = "Gender (disabled)".toUiText(),
            placeholderText = "Not available".toUiText()
        )
    }
}

@Preview(showBackground = true, name = "AppsDropdown - Generic + Custom content")
@Composable
private fun AppsDropdownGenericPreview() {
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val languages = listOf(
        Language("Kannada", "ಕನ್ನಡ", "🇮🇳 KA"),
        Language("Malayalam", "മലയാളം", "🇮🇳 KL"),
        Language("Tamil", "தமிழ்", "🇮🇳 TN"),
        Language("Telugu", "తెలుగు", "🇮🇳 AP/TS"),
        Language("English", "English", "🌍")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        AppsDropdown(
            options = languages,
            selectedIndex = selectedIndex,
            onItemSelected = { selectedIndex = it },
            labelText = "Preferred Language".toUiText(),
            placeholderText = "Choose language".toUiText(),
            matchFieldWidth = true,
            maxMenuHeight = 280.dp,
            selectedItemExtractor = {
                languages[selectedIndex].englishName.toUiText()
            },
            itemContent = { lang, isSelected ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = lang.flag,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = lang.englishName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) Kolt.colors.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = lang.nativeName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        )

        // With leading icon example
        val list = listOf("Light", "Dark", "System")
        AppsDropdown(
            options = list,
            selectedIndex = selectedIndex,
            onItemSelected = { selectedIndex = it },
            labelText = "Theme Mode".toUiText(),
            placeholderText = "Preferred theme".toUiText(),
            leadingIcon = {
                Icon(Icons.Default.DarkMode, contentDescription = null)
            }, selectedItemExtractor = {
                list[selectedIndex].toUiText()
            },
            itemContent = { mode, isSelected ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = when (mode) {
                            "Light" -> Icons.Default.LightMode
                            "Dark" -> Icons.Default.DarkMode
                            else -> Icons.Default.Settings
                        },
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(mode)
                }
            }
        )
    }
}

private data class Language(
    val englishName: String,
    val nativeName: String,
    val flag: String
)