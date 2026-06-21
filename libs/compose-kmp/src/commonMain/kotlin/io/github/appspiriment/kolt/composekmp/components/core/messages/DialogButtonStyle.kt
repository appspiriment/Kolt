package io.github.appspiriment.kolt.composekmp.components.core.messages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import io.github.appspiriment.kolt.composekmp.components.core.buttons.types.ButtonStyle


data class DialogButtonStyle(
    val positiveStyle: ButtonStyle,
    val negativeStyle: ButtonStyle,
    val arrangement: Arrangement.Horizontal = Arrangement.SpaceAround
) {
    companion object {
        @Composable
        fun primary(
            positiveStyle: ButtonStyle = ButtonStyle.primary(),
            negativeStyle: ButtonStyle = ButtonStyle.outlined(),
            arrangement: Arrangement.Horizontal = Arrangement.SpaceAround
        ) = DialogButtonStyle(
            positiveStyle = positiveStyle,
            negativeStyle = negativeStyle,
            arrangement = arrangement
        )

        @Composable
        fun outlined(
            positiveStyle: ButtonStyle = ButtonStyle.outlined(),
            negativeStyle: ButtonStyle = ButtonStyle.outlined(),
            arrangement: Arrangement.Horizontal = Arrangement.SpaceAround
        ) = DialogButtonStyle(
            positiveStyle = positiveStyle,
            negativeStyle = negativeStyle,
            arrangement = arrangement
        )

        @Composable
        fun transparent(
            positiveStyle: ButtonStyle = ButtonStyle.transparent(),
            negativeStyle: ButtonStyle = ButtonStyle.transparent(),
            arrangement: Arrangement.Horizontal = Arrangement.SpaceAround
        ) = DialogButtonStyle(
            positiveStyle = positiveStyle,
            negativeStyle = negativeStyle,
            arrangement = arrangement
        )

        @Composable
        fun default(
            positiveStyle: ButtonStyle = ButtonStyle.primary(),
            negativeStyle: ButtonStyle = ButtonStyle.primary(),
            arrangement: Arrangement.Horizontal = Arrangement.End
        ) = DialogButtonStyle(
            positiveStyle = positiveStyle,
            negativeStyle = negativeStyle,
            arrangement = arrangement
        )
    }
}
