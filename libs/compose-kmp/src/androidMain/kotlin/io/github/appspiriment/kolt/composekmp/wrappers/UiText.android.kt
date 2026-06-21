package io.github.appspiriment.kolt.composekmp.wrappers

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.fromHtml

@Composable
actual fun UiText.asString(): String {
    return asString(LocalContext.current)
}

@Composable
actual fun UiText.asStringList(): List<String> {
    return asStringList(LocalContext.current)
}

@Composable
actual fun UiText.asAnnotatedString(formatHtml: Boolean): AnnotatedString {
    return asAnnotatedString(LocalContext.current, formatHtml)
}

fun UiText.asString(context: Context): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.DynamicAnnotatedString -> value.text
        is UiText.StringArrayResource -> context.resources.getStringArray(resId).joinToString()
        is UiText.StringResource -> {
            val resolvedArgs = args.map { if (it is UiText) it.asString(context) else it }.toTypedArray()
            context.getString(resId, *resolvedArgs)
        }
        is UiText.PluralResource -> {
            val resolvedArgs = args.map { if (it is UiText) it.asString(context) else it }.toTypedArray()
            context.resources.getQuantityString(resId, quantity, *resolvedArgs)
        }
    }
}

fun UiText.asStringList(context: Context): List<String> {
    return when (this) {
        is UiText.StringArrayResource -> context.resources.getStringArray(resId).toList()
        else -> listOf(asString(context))
    }
}

fun UiText.asText(context: Context) = if (isAnnotatedString) asAnnotatedString(context) else asString(context)

private fun getAnnotatedString(text: String, formatHtml: Boolean = false): AnnotatedString {
    return if (formatHtml) AnnotatedString.fromHtml(text) else buildAnnotatedString { append(text) }
}

fun UiText.asAnnotatedString(context: Context, formatHtml: Boolean = false): AnnotatedString {
    return when (this) {
        is UiText.DynamicString -> getAnnotatedString(value, formatHtml)
        is UiText.StringResource -> getAnnotatedString(context.getString(resId, *args), formatHtml)
        is UiText.PluralResource -> getAnnotatedString(context.resources.getQuantityString(resId, quantity, *args), formatHtml)
        is UiText.DynamicAnnotatedString -> value
        else -> buildAnnotatedString { append(asString(context)) }
    }
}

fun UiText.isBlank(context: Context): Boolean = asString(context).isBlank()
fun UiText.isEmpty(context: Context): Boolean = asString(context).isEmpty()
fun UiText.takeIfNoEmpty(context: Context) = takeIf { !isEmpty(context) }
fun UiText.takeIfNoBlank(context: Context) = takeIf { !isBlank(context) }

@Composable
fun uiTextResource(id: Int, vararg formatArgs: Any): UiText {
    return stringResource(id = id, *formatArgs).toUiText()
}

@Composable
fun uiPluralTextResource(id: Int, count: Int, vararg formatArgs: Any): UiText {
    return pluralStringResource(id = id, count = count, *formatArgs).toUiText()
}

@Composable
fun uiTextArrayResource(id: Int): List<UiText> {
    return stringArrayResource(id = id).map {
        it.toUiText()
    }
}
