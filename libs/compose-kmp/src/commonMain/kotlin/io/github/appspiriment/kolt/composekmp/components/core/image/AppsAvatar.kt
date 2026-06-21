package io.github.appspiriment.kolt.composekmp.components.core.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage

private val AvatarPalette = listOf(
    Color(0xFF1976D2), // Blue 700
    Color(0xFF388E3C), // Green 700
    Color(0xFFD32F2F), // Red 700
    Color(0xFF7B1FA2), // Purple 700
    Color(0xFF0097A7), // Cyan 700
    Color(0xFFF57C00), // Orange 700
    Color(0xFF303F9F), // Indigo 700
    Color(0xFF00796B), // Teal 700
    Color(0xFFE64A19), // Deep Orange 700
    Color(0xFF5D4037), // Brown 700
    Color(0xFF455A64), // Blue Grey 700
    Color(0xFF689F38), // Light Green 700
)

@Composable
fun AppsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    image: UiImage? = null,
    size: Dp = Kolt.sizes.iconXLarge,
    textStyle: TextStyle = Kolt.typography.titleMedium,
    backgroundColor: Color? = null,
    contentColor: Color = Color.White,
    showShimmerWhileLoading: Boolean = true,
) {
    val initials = remember(name) { extractInitials(name) }
    val bgColor = backgroundColor ?: remember(name) { avatarColor(name) }
    val circleMod = modifier.size(size).clip(CircleShape)

    when {
        image == null -> {
            InitialsCircle(initials, bgColor, contentColor, textStyle, circleMod)
        }

        image is UiImage.RemoteImage -> {
            RemoteAvatarImage(
                image = image,
                name = name,
                initials = initials,
                bgColor = bgColor,
                contentColor = contentColor,
                textStyle = textStyle,
                modifier = circleMod,
                showShimmerWhileLoading = showShimmerWhileLoading,
            )
        }

        else -> {
            AppsImage(
                image = image,
                modifier = circleMod,
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
expect fun RemoteAvatarImage(
    image: UiImage.RemoteImage,
    name: String,
    initials: String,
    bgColor: Color,
    contentColor: Color,
    textStyle: TextStyle,
    modifier: Modifier,
    showShimmerWhileLoading: Boolean,
)

@Composable
internal fun InitialsCircle(
    initials: String,
    backgroundColor: Color,
    contentColor: Color,
    textStyle: TextStyle,
    modifier: Modifier,
) {
    Box(
        modifier = modifier.background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        InitialsContent(initials, contentColor, textStyle)
    }
}

@Composable
internal fun InitialsContent(
    initials: String,
    contentColor: Color,
    textStyle: TextStyle,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AppspirimentText(
            text = initials,
            style = textStyle,
            color = contentColor,
        )
    }
}

private fun extractInitials(name: String, maxLength: Int = 2): String {
    val words = name.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(maxLength).uppercase()
        else -> words.take(maxLength).map { it.first() }.joinToString("").uppercase()
    }
}

private fun avatarColor(name: String): Color {
    val index = name.trim().lowercase().hashCode().let {
        ((it % AvatarPalette.size) + AvatarPalette.size) % AvatarPalette.size
    }
    return AvatarPalette[index]
}
