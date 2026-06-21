package io.github.appspiriment.kolt.composeutils.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

/**
 * Remembers a stable lambda for a callback with one parameter.
 * This function prevents unnecessary recompositions of child composables
 * that accept a lambda as a parameter.
 *
 * The returned lambda is stable and will only be recreated if the `onEvent`
 * function instance itself changes.
 */
@Composable
fun <T> ((T) -> Unit).stabilize(): (T) -> Unit {
    return remember(this) { { event: T -> this(event) } }
}

/**
 * Remembers a stable lambda for a callback with no parameters.
 * This function prevents unnecessary recompositions of child composables
 * that accept a lambda as a parameter.
 */
@Composable
fun (() -> Unit).stabilize(): () -> Unit {
    return remember(this) { { this() } }
}

/**
 * Remembers a stable lambda for a callback with no parameters.
 * This function prevents unnecessary recompositions of child composables
 * that accept a lambda as a parameter.
 */
@Composable
fun <R> (() -> R).stabilizeLambda(): () -> R {
    return remember(this) { { this() } }
}

@Composable
fun <T, R> ((T) -> R).stabilizeLambda(): (T) -> R {
    return remember(this) { { event: T -> this(event) } }
}

