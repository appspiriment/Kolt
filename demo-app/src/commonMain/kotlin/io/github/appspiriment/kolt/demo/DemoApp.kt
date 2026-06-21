package io.github.appspiriment.kolt.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications

// KMP Components
import io.github.appspiriment.kolt.composekmp.components.containers.TextTitledCardView
import io.github.appspiriment.kolt.composekmp.components.containers.TitledCardView
import io.github.appspiriment.kolt.composekmp.components.containers.SmartPullToRefreshBox
import io.github.appspiriment.kolt.composekmp.components.core.*
import io.github.appspiriment.kolt.composekmp.components.core.badge.*
import io.github.appspiriment.kolt.composekmp.components.core.buttons.*
import io.github.appspiriment.kolt.composekmp.components.core.buttons.types.ButtonStyle
import io.github.appspiriment.kolt.composekmp.components.core.image.*
import io.github.appspiriment.kolt.composekmp.components.core.progress.*
import io.github.appspiriment.kolt.composekmp.components.core.text.*
import io.github.appspiriment.kolt.composekmp.theme.*
import io.github.appspiriment.kolt.composekmp.wrappers.*
import io.github.appspiriment.kolt.utils.state.AsyncState
import kotlinx.coroutines.delay

@Composable
fun DemoApp(
    platformName: String,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    platformContent: @Composable () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Layouts", "Buttons & Inputs", "Indicators & Images", "${platformName} Platform")

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Kolt.colors.mainSurface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppspirimentText(
                        text = "Kolt Showcase".toUiText(),
                        style = Kolt.typography.titleLarge.bold,
                        color = Kolt.colors.primary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onThemeToggle) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Toggle Theme",
                                tint = if (isDarkTheme) Color(0xFFFBC02D) else Kolt.colors.subText
                            )
                        }
                        Badge(
                            containerColor = Kolt.colors.primary.copy(alpha = 0.15f),
                            contentColor = Kolt.colors.primary
                        ) {
                            Text(
                                text = platformName,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Kolt.colors.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Kolt.colors.mainSurface,
                contentColor = Kolt.colors.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTab) {
                    0 -> LayoutsShowcase()
                    1 -> ButtonsInputsShowcase()
                    2 -> IndicatorsImagesShowcase()
                    else -> Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        platformContent()
                    }
                }
            }
        }
    }
}

@Composable
fun LayoutsShowcase() {
    var isRefreshing by remember { mutableStateOf(false) }

    SmartPullToRefreshBox(
        isExternallyRefreshing = isRefreshing,
        onRefreshTriggered = {
            isRefreshing = true
            delay(1500)
            isRefreshing = false
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header note about pull to refresh
            Card(
                colors = CardDefaults.cardColors(containerColor = Kolt.colors.infoContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Kolt.colors.accentedBlue)
                    Text(
                        "Swipe down from the top of this tab to test the SmartPullToRefreshBox!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Kolt.colors.onInfoContainer
                    )
                }
            }

            // --- 1. TextTitledCardView & Smooth Corners ---
            TextTitledCardView(
                title = "TextTitledCardView & Smooth Corners".toUiText(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var smoothness by remember { mutableStateOf(0.55f) }
                    var radius by remember { mutableStateOf(24f) }

                    AppspirimentText(
                        text = "Adjust smoothness & corner radius to see G2 continuous curves:".toUiText(),
                        style = Kolt.typography.textSmall,
                        color = Kolt.colors.onMainSurface.copy(alpha = 0.7f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(
                                    color = Kolt.colors.primary.copy(alpha = 0.1f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(radius.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Standard", style = MaterialTheme.typography.labelSmall)
                        }

                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(
                                    color = Kolt.colors.primary,
                                    shape = SmoothCornerShape(cornerRadius = radius.dp, smoothness = smoothness)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G2 Squircle", color = Kolt.colors.onPrimary, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Smoothness: ${(smoothness * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                            Slider(value = smoothness, onValueChange = { smoothness = it })
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Radius: ${radius.toInt()} dp", style = MaterialTheme.typography.bodySmall)
                            Slider(value = radius, onValueChange = { radius = it }, valueRange = 4f..48f)
                        }
                    }
                }
            }

            // --- 2. AppsAccordion ---
            TextTitledCardView(
                title = "AppsAccordion (Collapsible panels)".toUiText(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    var expandedA by remember { mutableStateOf(false) }
                    var expandedB by remember { mutableStateOf(false) }

                    AppsAccordion(
                        title = "Click to Expand Panel A".toUiText(),
                        expanded = expandedA,
                        onExpandedChange = { expandedA = it }
                    ) {
                        Text(
                            text = "Panel A expands with G2 corners, rotating chevron, and fade transitions.",
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    AppsAccordion(
                        title = "Click to Expand Panel B".toUiText(),
                        subtitle = "With subtitle support".toUiText(),
                        expanded = expandedB,
                        onExpandedChange = { expandedB = it }
                    ) {
                        Text(
                            text = "Panel B contains custom text and options.",
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // --- 3. AppsDivider & Spacers ---
            TextTitledCardView(
                title = "AppsDivider & Spacers".toUiText(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Horizontal Spacer (16dp) + Horizontal Divider:")
                    VerticalSpacer(16.dp)
                    AppsDivider(color = Kolt.colors.primary.copy(alpha = 0.5f), thickness = 2.dp)
                    VerticalSpacer(8.dp)
                    Text("Row with Vertical Spacer and Vertical Divider:")
                    Row(
                        modifier = Modifier.height(40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Left")
                        HorizontalSpacer(16.dp)
                        AppsVerticalDivider(color = Kolt.colors.primary, thickness = 1.dp)
                        HorizontalSpacer(16.dp)
                        Text("Right")
                    }
                }
            }

            // --- 4. TitledCardView (Generic Slots) ---
            TitledCardView(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Kolt.colors.secondaryCardContainer).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Kolt.colors.secondary)
                        AppspirimentText(
                            text = "Generic TitledCardView".toUiText(),
                            style = Kolt.typography.titleMedium.bold,
                            color = Kolt.colors.onSecondaryCardContainer
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("This card showcases TitledCardView using the custom title slot directly, allowing custom layouts in the title header block (like adding icons).")
                }
            }
        }
    }
}

@Composable
fun ButtonsInputsShowcase() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. Buttons Showcase ---
        TextTitledCardView(
            title = "Buttons & Actions".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                var buttonsEnabled by remember { mutableStateOf(true) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enable Buttons", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = buttonsEnabled, onCheckedChange = { buttonsEnabled = it })
                }

                Text("Standard Buttons:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppsButton(
                        text = "Primary".toUiText(),
                        enabled = buttonsEnabled,
                        buttonStyle = ButtonStyle.primary(),
                        onClick = {}
                    )
                    AppsTonalButton(
                        text = "Tonal".toUiText(),
                        enabled = buttonsEnabled,
                        onClick = {}
                    )
                    AppsTextButton(
                        text = "Secondary".toUiText(),
                        enabled = buttonsEnabled,
                        buttonStyle = ButtonStyle.transparent(),
                        onClick = {}
                    )
                }

                Text("Icon & Outlined Buttons:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppsOutlinedButton(
                        text = "Outlined".toUiText(),
                        leadingIcon = Icons.Default.Home.toUiImage(),
                        enabled = buttonsEnabled,
                        onClick = {}
                    )
                    AppsIconButton(
                        icon = Icons.Default.Notifications.toUiImage(),
                        enabled = buttonsEnabled,
                        onClick = {}
                    )
                    AppsCircularButton(
                        icon = Icons.Default.Check.toUiImage(),
                        onClick = {}
                    )
                }

                Text("iOS-Style Buttons (No ripple, opacity fade):")
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppsIosButton(
                            text = "iOS Filled".toUiText(),
                            style = IosButtonStyle.Filled,
                            enabled = buttonsEnabled,
                            onClick = {}
                        )
                        AppsIosButton(
                            text = "iOS Tinted".toUiText(),
                            style = IosButtonStyle.Tinted,
                            enabled = buttonsEnabled,
                            onClick = {}
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppsIosButton(
                            text = "iOS Gray".toUiText(),
                            style = IosButtonStyle.Gray,
                            enabled = buttonsEnabled,
                            onClick = {}
                        )
                        AppsIosButton(
                            text = "iOS Plain".toUiText(),
                            style = IosButtonStyle.Plain,
                            enabled = buttonsEnabled,
                            onClick = {}
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AppsIosButton(
                            text = "iOS with Icon".toUiText(),
                            leadingIcon = Icons.Default.Star.toUiImage(),
                            style = IosButtonStyle.Tinted,
                            enabled = buttonsEnabled,
                            onClick = {}
                        )
                    }
                }

                Text("Image Button:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppsImageButton(
                        icon = Icons.Default.Home.toUiImage(),
                        text = "Image Button".toUiText(),
                        enabled = buttonsEnabled,
                        onClick = {}
                    )
                }

                Text("Text / Link Buttons:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppsLinkButton(
                        text = "Link Button".toUiText(),
                        enabled = buttonsEnabled,
                        onClick = {}
                    )
                    TextButton(
                        text = "Plain Text".toUiText(),
                        enabled = buttonsEnabled,
                        buttonStyle = ButtonStyle.transparent(),
                        onClick = {}
                    )
                }
            }
        }

        // --- 2. AppsSlider & AppsProgressBar ---
        TextTitledCardView(
            title = "AppsSlider & AppsProgressBar".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                var sliderValue by remember { mutableStateOf(40f) }

                AppspirimentText(
                    text = "AppsSlider (Gradient fill, pulsing thumb, snaps):".toUiText(),
                    style = Kolt.typography.textSmall
                )
                AppsSlider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth()
                )

                AppspirimentText(
                    text = "AppsProgressBar (Synced):".toUiText(),
                    style = Kolt.typography.textSmall
                )
                AppsProgressBar(
                    progress = sliderValue / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // --- 3. AppsRatingBar ---
        TextTitledCardView(
            title = "AppsRatingBar".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                var currentRating by remember { mutableStateOf(3.5f) }
                var allowHalfSelection by remember { mutableStateOf(true) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Allow Half Selection", style = MaterialTheme.typography.bodySmall)
                    Switch(checked = allowHalfSelection, onCheckedChange = { allowHalfSelection = it })
                }

                AppspirimentText(
                    text = "Interactive (Value: $currentRating):".toUiText(),
                    style = Kolt.typography.textSmall
                )
                AppsRatingBar(
                    rating = currentRating,
                    readOnly = false,
                    allowHalfSelection = allowHalfSelection,
                    onRatingChange = { currentRating = it },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text("Read-only Display: $currentRating / 5.0")
                AppsRatingBar(
                    rating = currentRating,
                    readOnly = true,
                    allowHalfStars = true,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        // --- 4. AppsStepper ---
        TextTitledCardView(
            title = "AppsStepper (Wizard Flows)".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                var activeStep by remember { mutableStateOf(0) }
                val steps = listOf(
                    Step("Verification".toUiText(), "Verify details".toUiText()),
                    Step("Payment".toUiText(), "Submit details".toUiText()),
                    Step("Confirm".toUiText(), "Accept receipt".toUiText())
                )

                val stepperSteps = steps.mapIndexed { idx, s ->
                    val state = when {
                        idx < activeStep -> StepState.COMPLETED
                        idx == activeStep -> StepState.ACTIVE
                        else -> StepState.INACTIVE
                    }
                    s.copy(state = state)
                }

                AppsStepper(
                    steps = stepperSteps,
                    isVertical = false,
                    modifier = Modifier.fillMaxWidth()
                ) { index ->
                    val description = steps[index].description
                    val descStr = if (description != null) description.asString() else ""
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Current step task: $descStr")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AppsButton(
                        text = "Prev".toUiText(),
                        onClick = { if (activeStep > 0) activeStep-- },
                        buttonStyle = ButtonStyle.outlined(),
                        enabled = activeStep > 0
                    )
                    AppsButton(
                        text = "Next".toUiText(),
                        onClick = { if (activeStep < steps.size - 1) activeStep++ },
                        buttonStyle = ButtonStyle.primary(),
                        enabled = activeStep < steps.size - 1
                    )
                }
            }
        }
    }
}

@Composable
fun IndicatorsImagesShowcase() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. AppsStatusTag ---
        TextTitledCardView(
            title = "AppsStatusTag (Semantic Badges)".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppsStatusTag("Success".toUiText(), style = StatusStyle.Success)
                    AppsStatusTag("Warning".toUiText(), style = StatusStyle.Warning)
                    AppsStatusTag("Error".toUiText(), style = StatusStyle.Error)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AppsStatusTag("Info".toUiText(), style = StatusStyle.Info)
                    AppsStatusTag("Primary".toUiText(), style = StatusStyle.Primary)
                    AppsStatusTag("Neutral".toUiText(), style = StatusStyle.Neutral)
                }
            }
        }

        // --- 2. Badges & Tooltips ---
        TextTitledCardView(
            title = "Badges & Tooltips".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // BadgeBox (Count)
                BadgeBox(badge = BadgeState.Count(5)) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Kolt.colors.primary.copy(0.1f), shape = SmoothCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Inbox")
                    }
                }

                // BadgeBox (Dot)
                BadgeBox(badge = BadgeState.Dot) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Kolt.colors.secondary.copy(0.1f), shape = SmoothCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Alerts")
                    }
                }

                // Tooltip
                AppsTooltip(tooltip = "Press long to trigger tooltip".toUiText()) {
                    Box(
                        modifier = Modifier
                            .background(Kolt.colors.secondary.copy(0.2f), shape = SmoothCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("Hold Me")
                    }
                }
            }
        }

        // --- 3. Shimmer & Empty State ---
        TextTitledCardView(
            title = "Shimmer Skeleton & Empty States".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ShimmerListItem()
                ShimmerBox(
                    modifier = Modifier.fillMaxWidth().height(16.dp),
                    color = Kolt.colors.primary.copy(alpha = 0.4f)
                )
                
                Divider(color = Kolt.colors.dividerColor)

                AppsEmptyState(
                    title = "No Tasks Found".toUiText(),
                    message = "You have completed all items in your workflow.".toUiText(),
                    illustration = Icons.Default.Warning.toUiImage(),
                    action = "Refresh Feed".toUiText()
                )
            }
        }

        // --- 4. Progress & Loaders ---
        TextTitledCardView(
            title = "Progress & Loaders".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AppsCircularProgress(modifier = Modifier.size(36.dp))
                    Text("AppsCircularProgress (Indeterminate)")
                }
                
                AppsLinearProgress(modifier = Modifier.fillMaxWidth())
                Text("AppsLinearProgress (Indeterminate)", style = MaterialTheme.typography.bodySmall)
            }
        }

        // --- 5. Images, Icons & Avatars ---
        TextTitledCardView(
            title = "Images, Icons & Avatars".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppsAvatar(name = "Arun Shankar", size = 48.dp)
                    Text("AppsAvatar (Initials)")

                    CircleIconBox(
                        icon = Icons.Default.Home.toUiImage(),
                        backgroundColor = Kolt.colors.secondaryCardContainer,
                        circleSize = 48.dp
                    )
                    Text("CircleIconBox")
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppsIcon(
                        icon = Icons.Default.Notifications.toUiImage(),
                        size = 32.dp,
                        tint = Kolt.colors.primary
                    )
                    Text("AppsIcon (Tinted)")

                    AppsImage(
                        image = Icons.Default.Warning.toUiImage(),
                        modifier = Modifier.size(32.dp)
                    )
                    Text("AppsImage")
                }
            }
        }

        // --- 6. AsyncStateBox ---
        TextTitledCardView(
            title = "AsyncStateBox State Machine".toUiText(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                var asyncState by remember { mutableStateOf<AsyncState<String>>(AsyncState.Idle) }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { asyncState = AsyncState.Idle },
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Idle", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = { asyncState = AsyncState.Loading },
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Loading", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = { asyncState = AsyncState.Success("Successfully fetched dashboard data!") },
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Success", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = { asyncState = AsyncState.Error(message = "Failed to fetch data due to timeout.") },
                        modifier = Modifier.weight(1f).height(36.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Error", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Kolt.colors.mainSurface, shape = SmoothCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncStateBox(
                        state = asyncState,
                        onIdle = {
                            Text("State: Idle (Tap options above to transition state)", color = Kolt.colors.onMainSurface.copy(alpha = 0.6f))
                        },
                        onSuccess = { data ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Kolt.colors.success)
                                Text(data)
                            }
                        }
                    )
                }
            }
        }
    }
}
