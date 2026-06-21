package io.github.appspiriment.kolt.composeutils.components.core.modifier

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset

/**
 * Draws circle with a solid [color] behind the content.
 *
 * @param color The color of the circle.
 * @param padding The padding to be applied externally to the circular shape. It determines the spacing between
 * the edge of the circle and the content inside.
 *
 * @return Combined [Modifier] that first draws the background circle and then centers the layout.
 */
fun Modifier.circleBackground(color: Color, padding: Dp): Modifier {
    val backgroundModifier = drawBehind {
        drawCircle(color, size.width / 2f, center = Offset(size.width / 2f, size.height / 2f))
    }

    val layoutModifier = layout { measurable, constraints ->
        // Adjust the constraints by the padding amount
        val adjustedConstraints = constraints.offset(-padding.roundToPx())

        // Measure the composable with the adjusted constraints
        val placeable = measurable.measure(adjustedConstraints)

        // Get the current max dimension to assign width=height
        val currentHeight = placeable.height
        val currentWidth = placeable.width
        val newDiameter = maxOf(currentHeight, currentWidth) + padding.roundToPx() * 2

        // Assign the dimension and the center position
        layout(newDiameter, newDiameter) {
            // Place the composable at the calculated position
            placeable.placeRelative((newDiameter - currentWidth) / 2, (newDiameter - currentHeight) / 2)
        }
    }

    return this then backgroundModifier then layoutModifier
}


@Preview
@Composable
fun Preview_CircleBackground(){
    Box(Modifier.size(128.dp)){
        Text(text = "H", modifier = Modifier.circleBackground(Color.Red, 12.dp))
    }
}