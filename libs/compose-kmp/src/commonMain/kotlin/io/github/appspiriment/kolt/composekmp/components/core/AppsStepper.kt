package io.github.appspiriment.kolt.composekmp.components.core

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

/**
 * Represents the state of an individual step in the stepper.
 */
enum class StepState {
    ACTIVE,
    COMPLETED,
    INACTIVE
}

/**
 * Model class representing a single step configuration.
 *
 * @param title The title text of the step.
 * @param description Optional description text providing details for the step.
 * @param state The current state of the step. Defaults to [StepState.INACTIVE].
 */
data class Step(
    val title: UiText,
    val description: UiText? = null,
    val state: StepState = StepState.INACTIVE
)

/**
 * A wizard/progress stepper layout representing progress through sequential ordered nodes.
 * Supports both horizontal and vertical layouts.
 *
 * @param steps The list of steps to represent.
 * @param modifier The modifier to apply to the stepper layout.
 * @param isVertical True for column alignment, false for row alignment.
 * @param activeColor Color representing active steps (pulsing/scale node).
 * @param completedColor Color representing completed steps (check marked node).
 * @param inactiveColor Color representing inactive steps.
 * @param content Slot to host step content for the active step (or step-specific contents).
 */
@Composable
fun AppsStepper(
    steps: List<Step>,
    modifier: Modifier = Modifier,
    isVertical: Boolean = false,
    activeColor: Color = Kolt.colors.primary,
    completedColor: Color = Kolt.colors.success,
    inactiveColor: Color = Kolt.colors.dividerColor,
    content: @Composable (Int) -> Unit
) {
    if (isVertical) {
        Column(modifier = modifier) {
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(36.dp)
                    ) {
                        StepNode(
                            step = step,
                            index = index,
                            activeColor = activeColor,
                            completedColor = completedColor,
                            inactiveColor = inactiveColor
                        )

                        if (index < steps.lastIndex) {
                            val lineProgress by animateFloatAsState(
                                targetValue = if (step.state == StepState.COMPLETED) 1f else 0f,
                                animationSpec = tween(durationMillis = 300)
                            )
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(64.dp)
                                    .background(inactiveColor.copy(alpha = 0.3f), shape = CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(lineProgress)
                                        .background(completedColor, shape = CircleShape)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 24.dp)
                    ) {
                        AppspirimentText(
                            text = step.title,
                            style = Kolt.typography.textMedium.semiBold,
                            color = when (step.state) {
                                StepState.INACTIVE -> inactiveColor
                                else -> Kolt.colors.onMainSurface
                            }
                        )

                        if (step.description != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            AppspirimentText(
                                text = step.description,
                                style = Kolt.typography.bodyXSmall,
                                color = Kolt.colors.onMainSurface.copy(alpha = 0.6f)
                            )
                        }

                        if (step.state == StepState.ACTIVE) {
                            Spacer(modifier = Modifier.height(16.dp))
                            content(index)
                        }
                    }
                }
            }
        }
    } else {
        // Horizontal Stepper
        Column(modifier = modifier) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, step ->
                    // Column containing the Step Node + Title + Subtitle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Left spacing to center node or line
                            if (index > 0) {
                                val prevStep = steps[index - 1]
                                val lineProgress by animateFloatAsState(
                                    targetValue = if (prevStep.state == StepState.COMPLETED) 1f else 0f,
                                    animationSpec = tween(durationMillis = 300)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .background(inactiveColor.copy(alpha = 0.3f), shape = CircleShape)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(lineProgress)
                                            .background(completedColor, shape = CircleShape)
                                    )
                                }
                            }

                            StepNode(
                                step = step,
                                index = index,
                                activeColor = activeColor,
                                completedColor = completedColor,
                                inactiveColor = inactiveColor
                            )

                            // Right spacing
                            if (index < steps.lastIndex) {
                                val lineProgress by animateFloatAsState(
                                    targetValue = if (step.state == StepState.COMPLETED) 1f else 0f,
                                    animationSpec = tween(durationMillis = 300)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .background(inactiveColor.copy(alpha = 0.3f), shape = CircleShape)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(lineProgress)
                                            .background(completedColor, shape = CircleShape)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        AppspirimentText(
                            text = step.title,
                            style = Kolt.typography.textSmall.semiBold,
                            color = when (step.state) {
                                StepState.INACTIVE -> inactiveColor
                                else -> Kolt.colors.onMainSurface
                            },
                            textAlign = TextAlign.Center
                        )

                        if (step.description != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            AppspirimentText(
                                text = step.description,
                                style = Kolt.typography.bodyXXXSmall,
                                color = Kolt.colors.onMainSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // In horizontal mode, show active step content below
            val activeIndex = steps.indexOfFirst { it.state == StepState.ACTIVE }.coerceAtLeast(0)
            content(activeIndex)
        }
    }
}

@Composable
private fun StepNode(
    step: Step,
    index: Int,
    activeColor: Color,
    completedColor: Color,
    inactiveColor: Color
) {
    val scale = if (step.state == StepState.ACTIVE) {
        val infiniteTransition = rememberInfiniteTransition()
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
        pulseScale
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .size(32.dp)
            .background(
                color = when (step.state) {
                    StepState.COMPLETED -> completedColor
                    StepState.ACTIVE -> activeColor.copy(alpha = 0.15f)
                    StepState.INACTIVE -> Color.Transparent
                },
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = when (step.state) {
                    StepState.COMPLETED -> completedColor
                    StepState.ACTIVE -> activeColor
                    StepState.INACTIVE -> inactiveColor.copy(alpha = 0.5f)
                },
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        when (step.state) {
            StepState.COMPLETED -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            StepState.ACTIVE -> {
                AppspirimentText(
                    text = UiText.DynamicString((index + 1).toString()),
                    style = Kolt.typography.bodyXSmall.semiBold,
                    color = activeColor
                )
            }
            StepState.INACTIVE -> {
                AppspirimentText(
                    text = UiText.DynamicString((index + 1).toString()),
                    style = Kolt.typography.bodyXSmall,
                    color = inactiveColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}
