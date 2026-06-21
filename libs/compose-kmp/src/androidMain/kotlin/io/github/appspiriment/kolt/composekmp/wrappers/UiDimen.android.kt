package io.github.appspiriment.kolt.composekmp.wrappers

import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
actual fun UiDimen.asDp(): Dp {
    val context = LocalContext.current
    return asDp(context = context)
}

fun UiDimen.asDp(context: Context): Dp {
    return when (this) {
        is UiDimen.DynamicDp -> value
        is UiDimen.DimenResource -> context.resources.getDimension(resId).dp
        is UiDimen.DynamicTextUnit -> throw RuntimeException("For Dp, DynamicTextUnit type should not be used")
    }
}

@Composable
actual fun UiDimen.asSp(): TextUnit {
    val context = LocalContext.current
    return asSp(context = context)
}

fun UiDimen.asSp(context: Context): TextUnit {
    return when (this) {
        is UiDimen.DynamicTextUnit -> value
        is UiDimen.DimenResource -> context.resources.getDimension(resId).sp
        is UiDimen.DynamicDp -> throw RuntimeException("For Sp, DynamicDp type should not be used")
    }
}

object DimensionUtils {
    fun dimenResToDp(context: Context, dimenResId: Int): Dp? {
        return try {
            val px = context.resources.getDimension(dimenResId)
            px.toDp(context)
        } catch (e: Resources.NotFoundException) {
            println("Error: Dimension resource not found: $dimenResId")
            null
        }
    }

    fun dimenResToSp(context: Context, dimenResId: Int): TextUnit? {
        return try {
            val px = context.resources.getDimension(dimenResId)
            px.toSp(context)
        } catch (e: Resources.NotFoundException) {
            println("Error: Dimension resource not found: $dimenResId")
            null
        }
    }

    private fun Float.toDp(context: Context): Dp {
        return (this / context.resources.displayMetrics.density).dp
    }

    private fun Float.toSp(context: Context): TextUnit {
        return (this / context.resources.displayMetrics.scaledDensity).sp
    }

    @Composable
    fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }
}

@Composable
@ReadOnlyComposable
fun textSizeResource(id: Int): TextUnit {
    return dimensionResource(id = id).value.sp
}
