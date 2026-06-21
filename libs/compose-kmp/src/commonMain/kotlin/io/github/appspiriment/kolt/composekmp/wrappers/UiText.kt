package io.github.appspiriment.kolt.composekmp.wrappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString

/**
 * A sealed class to handle different types of text in Compose UI.
 *
 * This class allows you to represent text as a raw string, a string resource ID,
 * a quantity string resource ID, or an AnnotatedString. It provides a unified way to manage text in your UI,
 * regardless of its source or complexity.
 */
sealed class UiText {
    companion object {
        val Empty: UiText = DynamicString("")
    }

    /**
     * Represents a raw string.
     *
     * @property value The raw string value.
     */
    data class DynamicString(val value: String) : UiText()

    /**
     * Represents a string resource ID.
     *
     * @property resId The string resource ID.
     * @property args Optional arguments to format the string. These can be raw values or other [UiText] instances.
     */
    class StringResource(
        val resId: Int,
        vararg val args: Any
    ) : UiText()

    /**
     * Represents a quantity string resource ID.
     *
     * @property resId The quantity string resource ID.
     * @property quantity The quantity used to select the correct plural form.
     * @property args Optional arguments to format the string. These can be raw values or other [UiText] instances.
     */
    class PluralResource(
        val resId: Int,
        val quantity: Int,
        vararg val args: Any
    ) : UiText()

    /**
     * Represents a string array resource ID.
     *
     * @property resId The string array resource ID.
     */
    data class StringArrayResource(val resId: Int) : UiText()

    /**
     * Represents an AnnotatedString.
     *
     * @property value The AnnotatedString value.
     */
    data class DynamicAnnotatedString(val value: AnnotatedString) : UiText()

    val isAnnotatedString = this is DynamicAnnotatedString

    override fun equals(other: Any?): Boolean {
        return when {
            this === other -> true
            other !is UiText -> false
            this is DynamicString && other is DynamicString -> this.value == other.value
            this is DynamicAnnotatedString && other is DynamicAnnotatedString -> this.value == other.value
            this is StringResource && other is StringResource -> this.resId == other.resId && this.args.contentEquals(other.args)
            this is PluralResource && other is PluralResource -> this.resId == other.resId && this.quantity == other.quantity && this.args.contentEquals(other.args)
            this is StringArrayResource && other is StringArrayResource -> this.resId == other.resId
            else -> false
        }
    }

    override fun hashCode(): Int {
        return when (this) {
            is DynamicString -> value.hashCode()
            is StringResource -> resId.hashCode() + args.contentHashCode()
            is PluralResource -> resId.hashCode() + quantity.hashCode() + args.contentHashCode()
            is StringArrayResource -> resId.hashCode()
            is DynamicAnnotatedString -> value.hashCode()
        }
    }

    /**
     * Checks if the UiText is blank (empty or contains only whitespace) without a context.
     * This is only applicable to DynamicString and DynamicAnnotatedString.
     *
     * @return True if the UiText is blank, false otherwise.
     */
    fun isBlank(): Boolean {
        return when (this) {
            is DynamicString -> value.isBlank()
            is DynamicAnnotatedString -> value.text.isBlank()
            else -> throw Exception("For non-DynamicString values use isEmpty with context")
        }
    }

    /**
     * Checks if the UiText is empty without a context.
     * This is only applicable to DynamicString and DynamicAnnotatedString.
     *
     * @return True if the UiText is empty, false otherwise.
     */
    fun isEmpty(): Boolean {
        return when (this) {
            is DynamicString -> value.isEmpty()
            is DynamicAnnotatedString -> value.text.isEmpty()
            else -> throw Exception("For non-DynamicString values use isEmpty with context")
        }
    }

    fun takeIfNoEmpty() = takeIf { !isEmpty() }
    fun takeIfNoBlank() = takeIf { !isBlank() }
}

fun String.toUiText(): UiText = UiText.DynamicString(this)
fun Int.toUiText(): UiText = UiText.DynamicString("$this")
fun List<String>.toUiText(): List<UiText> = map { UiText.DynamicString(it) }
fun AnnotatedString.toUiText(): UiText = UiText.DynamicAnnotatedString(this)
fun CharSequence.toUiText(): UiText = UiText.DynamicString(this.toString())

fun String?.toUiTextOrEmpty(): UiText = this?.toUiText() ?: UiText.Empty
fun Int?.toUiTextOrEmpty(): UiText = this?.toUiText() ?: UiText.Empty
fun List<String?>?.toUiTextOrEmpty(): List<UiText> = this?.map { it?.toUiText() ?: UiText.Empty } ?: emptyList()
fun AnnotatedString?.toUiTextOrEmpty(): UiText = this?.toUiText() ?: UiText.Empty
fun CharSequence?.toUiTextOrEmpty(): UiText = this?.toUiText() ?: UiText.Empty

fun String?.toUiTextOrElse(provider: () -> String): UiText = (this ?: provider()).toUiText()
fun Int?.toUiTextOrElse(provider: () -> String): UiText = this?.toUiText() ?: provider().toUiText()
fun List<String>?.toUiTextOrElse(provider: () -> List<String>): List<UiText> = this?.map { it.toUiText() } ?: provider().map { it.toUiText() }
fun AnnotatedString?.toUiTextOrElse(provider: () -> String): UiText = this?.toUiText() ?: provider().toUiText()
fun CharSequence?.toUiTextOrElse(provider: () -> String): UiText = this?.toUiText() ?: provider().toUiText()

fun String?.toUiTextOr(provider: () -> UiText): UiText = this?.toUiText() ?: provider()
fun Int?.toUiTextOr(provider: () -> UiText): UiText = this?.toUiText() ?: provider()
fun List<String>?.toUiTextOr(provider: () -> List<UiText>): List<UiText> = this?.map { it.toUiText() } ?: provider().map { it }
fun AnnotatedString?.toUiTextOr(provider: () -> UiText): UiText = this?.toUiText() ?: provider()
fun CharSequence?.toUiTextOr(provider: () -> UiText): UiText = this?.toUiText() ?: provider()

@Composable
expect fun UiText.asString(): String

@Composable
expect fun UiText.asStringList(): List<String>

@Composable
expect fun UiText.asAnnotatedString(formatHtml: Boolean = false): AnnotatedString