package io.github.appspiriment.kolt.composeutils.utils


import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrowseGallery
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import io.github.appspiriment.kolt.composeutils.R
import io.github.appspiriment.kolt.composekmp.components.core.HorizontalSpacer
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiColor
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.toUiImage
import java.io.File

// ────────────────────────────────────────────────
// Models
// ────────────────────────────────────────────────

data class PhotoPickerActions(
    val openGallery: () -> Unit,
    val openCamera: () -> Unit
)

fun interface PhotoCropContract {
    fun onHandleCrop(sourceUri: Uri)
}

data class PhotoPickerConfig(
    val cameraIcon: UiImage = Icons.Default.Camera.toUiImage(),
    val galleryIcon: UiImage = Icons.Default.BrowseGallery.toUiImage(),
    val removeIcon: UiImage = Icons.Default.Delete.toUiImage(tint = UiColor.ColorResource(R.color.error)),
    val cameraText: UiText = UiText.StringResource(R.string.camera),
    val galleryText: UiText = UiText.StringResource(R.string.gallery),
    val removeText: UiText = UiText.StringResource(R.string.remove_photo),
    val sheetContainerColor: Color = Color.Unspecified,
    val onImageRemoved: (() -> Unit)? = null
)

sealed class PickerInteraction {
    data class Default(val config: PhotoPickerConfig = PhotoPickerConfig()) : PickerInteraction()
    data class Custom(val onClick: () -> Unit) : PickerInteraction()
}

// ────────────────────────────────────────────────
// rememberPhotoPicker – launcher factory
// ────────────────────────────────────────────────

@Composable
fun rememberPhotoPicker(
    onImagePicked: (Uri) -> Unit,
    onTriggerCrop: ((Uri) -> Unit)? = null
): PhotoPickerActions {
    val context = LocalContext.current
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val handleResult: (Uri) -> Unit = { uri ->
        if (onTriggerCrop != null) {
            onTriggerCrop(uri)
        } else {
            onImagePicked(uri)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let(handleResult) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) tempUri?.let(handleResult)
    }

    val cameraPermissionLauncher = rememberPermissionRequest(
        permissions = listOf(
            AppPermission(android.Manifest.permission.CAMERA, "Camera", isRequired = true)
        ),
        onActionGranted = {
            val uri = createTempCaptureUri(context)
            tempUri = uri
            cameraLauncher.launch(uri)
        }
    )

    return remember {
        PhotoPickerActions(
            openGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            openCamera = cameraPermissionLauncher
        )
    }
}

private fun createTempCaptureUri(context: Context): Uri {
    val file = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.appspiriment.fileprovider",
        file
    )
}

// ────────────────────────────────────────────────
// Main Composable – reusable circle image picker
// ────────────────────────────────────────────────

@Composable
fun AppCircleImagePicker(
    modifier: Modifier = Modifier,
    currentImage: Any?,                   // parent-controlled image (url, uri, resource, etc.)
    size: Dp = 120.dp,
    borderStroke: BorderStroke = BorderStroke(2.dp, Kolt.colors.primary),
    onImagePicked: (Uri) -> Unit = {},    // called when new image is selected (before crop)
    cropContract: PhotoCropContract? = null,
    interaction: PickerInteraction = PickerInteraction.Default(),
    imageContent: @Composable (Any?) -> Unit
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var localPickedUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberPhotoPicker(
        onImagePicked = { uri ->
            localPickedUri = uri
            onImagePicked(uri)
        },
        onTriggerCrop = { uri ->
            if (cropContract != null) {
                cropContract.onHandleCrop(uri)
            } else {
                localPickedUri = uri
                onImagePicked(uri)
            }
        }
    )

    // Decide what to show: parent's authoritative image wins after upload/sync
    // localPickedUri is only used for temporary preview during the current session
    val displayImage = currentImage ?: localPickedUri

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .border(borderStroke, CircleShape)
                .clickable {
                    when (interaction) {
                        is PickerInteraction.Default -> showBottomSheet = true
                        is PickerInteraction.Custom -> interaction.onClick()
                    }
                }
        ) {
            imageContent(displayImage)

            // Semi-transparent overlay with camera icon
            Surface(
                color = Color.Black.copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(size / 4)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val icon = when (interaction) {
                        is PickerInteraction.Default -> interaction.config.cameraIcon
                        else -> Icons.Default.Camera.toUiImage(tint = Kolt.uiColors.primary)
                    }
                    AppsIcon(icon = icon, modifier = Modifier.size(size / 8))
                }
            }
        }
    }

    if (showBottomSheet && interaction is PickerInteraction.Default) {
        ImageSourceBottomSheet(
            config = interaction.config,
            hasActiveImage = displayImage != null,
            onDismiss = { showBottomSheet = false },
            onCameraClick = {
                showBottomSheet = false
                picker.openCamera()
            },
            onGalleryClick = {
                showBottomSheet = false
                picker.openGallery()
            },
            onRemoveClick = {
                showBottomSheet = false
                localPickedUri = null
                interaction.config.onImageRemoved?.invoke()
            }
        )
    }
}

// ────────────────────────────────────────────────
// Bottom sheet & list item
// ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageSourceBottomSheet(
    config: PhotoPickerConfig,
    hasActiveImage: Boolean,
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (config.sheetContainerColor != Color.Unspecified)
            config.sheetContainerColor else Kolt.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 8.dp)
        ) {
            SourceItem(
                icon = config.cameraIcon,
                text = config.cameraText,
                textColor = Kolt.colors.onMainSurface,
                onClick = onCameraClick
            )
            SourceItem(
                icon = config.galleryIcon,
                text = config.galleryText,
                textColor = Kolt.colors.onMainSurface,
                onClick = onGalleryClick
            )

            if (config.onImageRemoved != null && hasActiveImage) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Kolt.colors.disabledText
                )
                SourceItem(
                    icon = config.removeIcon,
                    text = config.removeText,
                    textColor = Color.Red,
                    onClick = onRemoveClick
                )
            }
        }
    }
}

@Composable
private fun SourceItem(
    icon: UiImage,
    text: UiText,
    textColor: Color = Kolt.colors.onMainSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppsIcon(icon = icon, modifier = Modifier.size(24.dp))
        HorizontalSpacer(width = 16.dp)
        AppspirimentText(
            text = text,
            color = textColor,
            style = Kolt.typography.textMedium
        )
    }
}

// ────────────────────────────────────────────────
// FileProvider – avoids manifest merge conflicts
// Add to AndroidManifest.xml:
//     <provider
//         android:name=".utils.KoltFileProvider"
//         android:authorities="${applicationId}.appspiriment.fileprovider"
//         android:exported="false"
//         android:grantUriPermissions="true">
//         <meta-data
//             android:name="android.support.FILE_PROVIDER_PATHS"
//             android:resourceName="@xml/file_paths" />
//     </provider>
// ────────────────────────────────────────────────

class KoltFileProvider : FileProvider()