package io.github.appspiriment.kolt.composeutils.utils

import android.graphics.Rect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Previous state tracking ───────────────────────────────────────────────────

/**
 * Returns the previous value of [current] from the last composition in which it changed.
 * Returns null on first composition.
 *
 * Useful for animating between two known states (e.g., slide in from previous tab index).
 */
@Composable
fun <T> rememberPreviousState(current: T): T? {
    val previous = remember { mutableStateOf<T?>(null) }
    val last = remember { mutableStateOf(current) }
    if (last.value != current) {
        previous.value = last.value
        last.value = current
    }
    return previous.value
}

// ── Debounce / throttle ───────────────────────────────────────────────────────

/**
 * Returns a stable lambda that debounces [action] by [delayMillis].
 * Each call cancels any pending invocation and restarts the delay timer.
 *
 * Useful for search-as-you-type fields.
 */
@Composable
fun rememberDebounced(
    delayMillis: Long = 300L,
    action: () -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    val job = remember { mutableStateOf<Job?>(null) }
    return remember(action) {
        {
            job.value?.cancel()
            job.value = scope.launch {
                delay(delayMillis)
                action()
            }
        }
    }
}

/**
 * Returns a stable lambda that throttles [action] — at most once per [intervalMillis].
 * The first call goes through immediately; subsequent calls within the interval are dropped.
 *
 * Useful for button taps that should not fire twice on a double-tap.
 */
@Composable
fun rememberThrottled(
    intervalMillis: Long = 500L,
    action: () -> Unit,
): () -> Unit {
    val lastExecuted = remember { mutableStateOf(0L) }
    return remember(action) {
        {
            val now = System.currentTimeMillis()
            if (now - lastExecuted.value >= intervalMillis) {
                lastExecuted.value = now
                action()
            }
        }
    }
}

// ── Keyboard visibility ───────────────────────────────────────────────────────

/**
 * Observes whether the software keyboard is currently visible.
 *
 * Uses a `ViewTreeObserver` to compare the window's visible frame against the root view height.
 * Returns a [State<Boolean>] that updates on keyboard show/hide.
 *
 * Note: this is a heuristic — keyboards that occupy < 15% of screen height are treated as hidden.
 */
@Composable
fun rememberKeyboardState(): State<Boolean> {
    val isVisible = remember { mutableStateOf(false) }
    val view = LocalView.current

    DisposableEffect(view) {
        val listener = android.view.ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            isVisible.value = keypadHeight > screenHeight * 0.15f
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
        }
    }

    return isVisible
}
