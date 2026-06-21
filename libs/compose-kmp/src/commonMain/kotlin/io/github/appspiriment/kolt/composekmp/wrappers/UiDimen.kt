package io.github.appspiriment.kolt.composekmp.wrappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * A sealed class to handle different types of dimensions in Compose UI.
 *
 * This class allows you to represent dimensions as a raw Dp, a TextUnit, or a dimension resource ID.
 * It provides a unified way to manage dimensions in your UI, regardless of its source.
 */
sealed class UiDimen {
    /**
     * Represents a raw Dp value.
     *
     * @property value The raw Dp value.
     */
    data class DynamicDp(val value: Dp) : UiDimen() {
        companion object {
            val Zero = DynamicDp(0.dp)
            val Unspecified = DynamicDp(Dp.Unspecified)
        }
    }

    /**
     * Represents a raw TextUnit (sp) value.
     *
     * @property value The raw TextUnit (sp) value.
     */
    data class DynamicTextUnit(val value: TextUnit) : UiDimen() {
        companion object {
            val Unspecified = DynamicTextUnit(TextUnit.Unspecified)
        }
    }

    /**
     * Represents a dimension resource ID.
     *
     * @property resId The dimension resource ID.
     */
    class DimenResource(val resId: Int) : UiDimen()
}

/**
 * Extension function to convert a Dp to a UiDimen.
 */
fun Dp.toUiDimen() = UiDimen.DynamicDp(this)

/**
 * Extension function to convert a TextUnit to a UiDimen.
 */
fun TextUnit.toUiDimen() = UiDimen.DynamicTextUnit(this)

/**
 * Extension function to convert a dimension resource id to a UiDimen.
 */
fun Int.toUiDimenResource() = UiDimen.DimenResource(this)

/**
 * Composable function to get a UiDimen from a dimension resource ID.
 *
 * @param id The dimension resource ID.
 * @return The UiDimen representing the dimension resource.
 */
@Composable
fun uiDimenResource(id: Int): UiDimen {
    return UiDimen.DimenResource(id)
}

@Composable
expect fun UiDimen.asDp(): Dp

@Composable
expect fun UiDimen.asSp(): TextUnit
