package io.github.appspiriment.kolt.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.containers.TextTitledCardView
import io.github.appspiriment.kolt.composekmp.components.core.buttons.TextButton
import io.github.appspiriment.kolt.composekmp.components.core.buttons.types.ButtonStyle
import io.github.appspiriment.kolt.composekmp.theme.CompositionBaseProvider
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText
import io.github.appspiriment.kolt.composekmp.components.core.messages.MessageDialog
import io.github.appspiriment.kolt.composekmp.components.core.messages.AppsBanner
import io.github.appspiriment.kolt.composekmp.components.core.messages.BannerStyle
import io.github.appspiriment.kolt.composeutils.components.messages.showToast
import io.github.appspiriment.kolt.composekmp.components.core.messages.AppsBottomSheet
import io.github.appspiriment.kolt.composeutils.components.core.dropdowns.*
import io.github.appspiriment.kolt.composekmp.components.core.text.*
import io.github.appspiriment.kolt.composekmp.wrappers.toUiImage
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by remember { mutableStateOf(false) }
            CompositionBaseProvider(isDarkTheme = isDarkTheme) {
                DemoApp(
                    platformName = "Android",
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { isDarkTheme = !isDarkTheme }
                ) {
                    AndroidShowcaseContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidShowcaseContent() {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var textDropdownIndex by remember { mutableIntStateOf(-1) }
    var iconDropdownIndex by remember { mutableIntStateOf(-1) }
    var appsDropdownIndex by remember { mutableIntStateOf(-1) }

    val states = listOf(
        "Karnataka", "Kerala", "Tamil Nadu", "Andhra Pradesh", "Telangana"
    )
    val stateUiTexts = states.map { it.toUiText() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Dialogs & Sheet ---
        TextTitledCardView(
            title = "Native Overlay Controllers".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        text = "Show MessageDialog".toUiText(),
                        onClick = { showDialog = true },
                        buttonStyle = ButtonStyle.primary(),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        text = "Show BottomSheet".toUiText(),
                        onClick = { showSheet = true },
                        buttonStyle = ButtonStyle.outlined(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 2. Banners & Toast ---
        TextTitledCardView(
            title = "Banners & System Feedback".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppsBanner(
                    message = "Your active subscription is terminating in 3 days.".toUiText(),
                    style = BannerStyle.Warning,
                    actionText = "Renew Now".toUiText(),
                    onAction = { context.showToast("Subscription renewed!".toUiText()) }
                )

                TextButton(
                    text = "Trigger System Toast".toUiText(),
                    onClick = { context.showToast("This is a native Android toast!".toUiText()) },
                    buttonStyle = ButtonStyle.outlined(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- 3. Dropdowns (Android Only) ---
        TextTitledCardView(
            title = "Dropdown Spinners".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("AppsTextDropDown (Pill-style trigger):", style = MaterialTheme.typography.bodySmall)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppsTextDropDown(
                        items = stateUiTexts,
                        placeholderText = "Choose State".toUiText(),
                        containerColor = Kolt.colors.secondaryCardContainer,
                        onItemSelected = {
                            textDropdownIndex = it
                            context.showToast("Selected via text dropdown: ${states[it]}".toUiText())
                        }
                    )
                    Text(
                        text = if (textDropdownIndex != -1) "Selected: ${states[textDropdownIndex]}" else "None",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text("AppsIconDropDown (Icon trigger):", style = MaterialTheme.typography.bodySmall)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppsIconDropDown(
                        items = stateUiTexts,
                        icon = Icons.Default.Notifications.toUiImage(),
                        onItemSelected = {
                            iconDropdownIndex = it
                            context.showToast("Selected via icon dropdown: ${states[it]}".toUiText())
                        }
                    )
                    Text(
                        text = if (iconDropdownIndex != -1) "Selected: ${states[iconDropdownIndex]}" else "None",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text("AppsDropdown (Floating label):", style = MaterialTheme.typography.bodySmall)
                AppsDropdown(
                    options = stateUiTexts,
                    selectedIndex = appsDropdownIndex,
                    onItemSelected = {
                        appsDropdownIndex = it
                        context.showToast("Selected via AppsDropdown: ${states[it]}".toUiText())
                    },
                    labelText = "State Options".toUiText(),
                    placeholderText = "Select your home state".toUiText()
                )
            }
        }

        // --- 4. Android-Only Text Helpers ---
        TextTitledCardView(
            title = "Text Components & Helpers".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("AppsCopyableText (Click to copy):", style = MaterialTheme.typography.bodySmall)
                AppsCopyableText(
                    text = "support@appspiriment.com",
                    color = Kolt.colors.primary,
                    onCopied = { context.showToast("Copied to clipboard!".toUiText()) }
                )

                Text("AppsExpandableText (Read More):", style = MaterialTheme.typography.bodySmall)
                AppsExpandableText(
                    text = "Kolt provides high-quality convention plugins and utility libraries for Kotlin Multiplatform developers. By consolidating build logic, it enables teams to share structured setups easily and upgrade dependencies via standard version catalogs without rebuilding plugins.",
                    collapsedMaxLines = 2,
                    expandLabel = "Show more".toUiText(),
                    collapseLabel = "Show less".toUiText()
                )

                Text("AppsImageText (Aligned with Icon):", style = MaterialTheme.typography.bodySmall)
                AppsImageText(
                    text = "Kolt System Settings".toUiText(),
                    startingImage = Icons.Default.Home.toUiImage(),
                    startingImageHeight = 24.dp
                )
            }
        }

        // --- 5. Indic Script Rendering ---
        TextTitledCardView(
            title = "Indic Script Rendering (AppspirimentText)".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Standard Compose Text rendering Indic script (might fall back to default fonts):",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "കേരളം - ഹലോ സുഹൃത്തേ, സുഖമാണോ?",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "AppspirimentText component (correctly resolves Noto Sans Malayalam via theme):",
                    style = MaterialTheme.typography.bodySmall
                )
                AppspirimentText(
                    text = "കേരളം - ഹലോ സുഹൃത്തേ, സുഖമാണോ?".toUiText(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // MessageDialog state
        if (showDialog) {
            MessageDialog(
                title = "G2 Smooth Dialog".toUiText(),
                message = "This dialog uses the custom SmoothCornerShape(12.dp) for G2 continuous corners. It's built specifically for Android applications.".toUiText(),
                positiveText = "Accept".toUiText(),
                negativeText = "Dismiss".toUiText(),
                listener = { accepted ->
                    showDialog = false
                    context.showToast((if (accepted) "Accepted" else "Dismissed").toUiText())
                },
                onDismissRequest = { showDialog = false }
            )
        }

        // AppsBottomSheet state
        if (showSheet) {
            AppsBottomSheet(
                showSheet = showSheet,
                state = sheetState,
                title = "Interactive Bottom Sheet".toUiText(),
                dismissSheet = { showSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "This modal bottom sheet slides up gracefully and provides custom action triggers."
                    )
                    TextButton(
                        text = "Close Sheet".toUiText(),
                        onClick = { showSheet = false },
                        buttonStyle = ButtonStyle.primary()
                    )
                }
            }
        }
    }
}
