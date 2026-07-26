package io.github.appspiriment.kolt.composekmp.components.core.text.textfield

import io.github.appspiriment.kolt.composekmp.R
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

object ValidationRules {

    val required = ValidationRule(
        test = { it.isBlank() },
        message = UiText.StringResource(R.string.field_required)
    )

    val emailFormat = ValidationRule(
        test = { !it.contains("@") || !it.contains(".") },
        message = UiText.StringResource(R.string.invalid_email_format)
    )

    val phoneNumber = ValidationRule(
        test = { it.length != 10 || !it.all(Char::isDigit) },
        message = UiText.StringResource(R.string.invalid_phone_number)
    )

    val noSpecialChars = ValidationRule(
        test = { it.any { c -> !c.isLetterOrDigit() && c != ' ' } },
        message = UiText.StringResource(R.string.no_special_characters_allowed)
    )
}
