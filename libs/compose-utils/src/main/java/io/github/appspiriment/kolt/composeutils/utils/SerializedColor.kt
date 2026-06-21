package io.github.appspiriment.kolt.composeutils.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ColorSerializer : KSerializer<SerializedColor> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: SerializedColor) {
        encoder.encodeInt(value.value.toArgb())
    }

    override fun deserialize(decoder: Decoder): SerializedColor {
        return SerializedColor(Color(decoder.decodeInt()))
    }
}

@JvmInline
@Serializable(with = ColorSerializer::class) // <-- Applied here!
value class SerializedColor(val value: Color)

/**
 * Convenience extension property to convert a [Color] to a [SerializedColor].
 *
 * This allows for a more idiomatic assignment, for example:
 * `val myTheme = MyTheme(backgroundColor = Color.Red.serialized)`
 */
inline val Color.serialized: SerializedColor
    get() = SerializedColor(this)
