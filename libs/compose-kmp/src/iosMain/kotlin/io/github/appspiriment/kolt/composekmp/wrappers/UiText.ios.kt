package io.github.appspiriment.kolt.composekmp.wrappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString

@Composable
actual fun UiText.asString(): String {
    return when (this) {
        is UiText.DynamicString -> value
        is UiText.DynamicAnnotatedString -> value.text
        is UiText.StringArrayResource -> ""
        is UiText.StringResource -> {
            val resolvedArgs = args.map { if (it is UiText) it.asString() else it.toString() }.joinToString()
            "Resource($resId, $resolvedArgs)"
        }
        is UiText.PluralResource -> {
            val resolvedArgs = args.map { if (it is UiText) it.asString() else it.toString() }.joinToString()
            "Plural($resId, quantity=$quantity, $resolvedArgs)"
        }
    }
}

@Composable
actual fun UiText.asStringList(): List<String> {
    return when (this) {
        is UiText.StringArrayResource -> emptyList()
        else -> listOf(asString())
    }
}

@Composable
actual fun UiText.asAnnotatedString(formatHtml: Boolean): AnnotatedString {
    return when (this) {
        is UiText.DynamicAnnotatedString -> value
        is UiText.DynamicString -> buildAnnotatedString { append(value) }
        else -> buildAnnotatedString { append(asString()) }
    }
}
