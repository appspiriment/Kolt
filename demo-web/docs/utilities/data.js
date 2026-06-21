// Utility library reference — source: project-templates/docs/KOLT.md §6 + direct source inspection.
export const UTILITIES = [
    {
        id: 'utils',
        title: 'utils',
        coordinate: 'io.github.appspiriment.kolt:utils',
        platforms: 'KMP (commonMain + androidMain)',
        summary: 'Flow, list, string, and phone-number helpers shared across every target, plus Android-only date/context extensions.',
        intro: 'The core utility library provides fundamental helpers and extension functions for day-to-day Kotlin development across all platforms. It solves the issue of repeating boilerplate operations on collections, strings, flows, and phone numbers. Use it in common code to format phone numbers, capitalization, or emails, and on Android for network checking and date/time manipulation.',
        sections: [
            {
                heading: 'commonMain — AsyncState Machine',
                path: 'libs/utils/src/commonMain/kotlin/.../state/AsyncState.kt',
                code: `// A clean sealed class to model async operations (e.g. network requests) in ViewModels:
sealed class AsyncState<out T> {
    data object Idle : AsyncState<Nothing>()
    data object Loading : AsyncState<Nothing>()
    data class Success<T>(val data: T) : AsyncState<T>()
    data class Error(val throwable: Throwable) : AsyncState<Nothing>()
}

// Check state details in common code:
val state: AsyncState<User> = AsyncState.Loading
if (state is AsyncState.Success) {
    val user = state.data
}`,
            },
            {
                heading: 'commonMain — Coroutine Flow Lifecycles',
                path: 'libs/utils/src/commonMain/kotlin/.../extensions/FlowUtils.kt',
                code: `// Keeps flows active only when there are active UI subscribers (shares resources efficiently):
flow.stateInWhileSubscribed(
    scope = viewModelScope,
    initialValue = AsyncState.Idle
)

flow.shareInWhileSubscribed(scope = viewModelScope)

// Shorthand to debounce a flow and collect safely on the default dispatcher:
flow.debounceAndCollect(
    timeoutMillis = 300L,
    scope = viewModelScope
) { collectedValue ->
    processSearch(collectedValue)
}`,
            },
            {
                heading: 'commonMain — Collection & String Extensions',
                path: 'libs/utils/src/commonMain/kotlin/.../extensions/{ListUtils,StringExtns}.kt',
                code: `// 1. List operations
val updatedList = list.moveItem(fromIndex = 2, toIndex = 0)
val modifiedList = list.replaceFirst(predicate = { it.id == userId }) { user ->
    user.copy(name = "New Name")
}

// 2. String validations and formatting
val isEmailValid = "user@example.com".isValidEmail()  // returns Boolean
val isUrlValid = "https://github.com".isValidUrl()      // returns Boolean
val formatted = "kotlin multiplatform".capitalizeWords() // returns "Kotlin Multiplatform"`,
            },
            {
                heading: 'commonMain — Phone Number Formatting',
                path: 'libs/utils/src/commonMain/kotlin/.../extensions/PhoneNumberUtils.kt',
                code: `// Validates and formats international phone numbers:
val isValid = PhoneNumberUtils.isValid("+919876543210") // returns Boolean

// Standardizes formatting for database storage or displays:
val displayPhone = PhoneNumberUtils.format("+919876543210") // e.g., "+91 98765-43210"`,
            },
            {
                heading: 'androidMain — Context & System Intents',
                path: 'libs/utils/src/androidMain/kotlin/.../extensions/ContextExtensions.kt',
                code: `// Launches standard system intents on the Android host:
context.launchPlayStorePage(packageName = "com.example.app")

// Checks if the device has an active internet connection (requires ACCESS_NETWORK_STATE):
if (context.isNetworkAvailable()) {
    triggerSync()
}`,
            },
            {
                heading: 'androidMain — Currency & Locale Formatting',
                path: 'libs/utils/src/androidMain/kotlin/.../extensions/FormatUtils.kt',
                code: `// Formats numbers into localized currency structures based on ISO codes:
val price = FormatUtils.formatCurrency(amount = 250.50, currencyCode = "USD") // returns "$250.50"
val inRupees = FormatUtils.formatCurrency(amount = 1000.0, currencyCode = "INR") // returns "₹1,000.00"`,
            },
            {
                heading: 'androidMain — Date & Relative Time Extensions',
                path: 'libs/utils/src/androidMain/kotlin/.../extensions/LocalDateTimeExtensions.kt',
                code: `// Format dates and times using local Android locales:
val localDate = LocalDate.now()
val localDateTime = LocalDateTime.now()

val displayDate = localDate.formatDisplay()      // returns e.g. "12 Jun 2026"
val displayDateTime = localDate.formatDateTime() // returns e.g. "12 Jun 2026, 15:30"
val customDate = FormatUtils.formatDate(localDate, "yyyy/MM/dd")

// Unix epoch conversion helpers:
val epochMillis = localDate.toEpochMillis()
val reconstructedDate = epochMillis.toLocalDateTime()

// Converts date/time stamps into human-readable relative intervals:
val agoText = localDateTime.toRelativeTime() // returns e.g. "3 hours ago" or "Just now"`,
            },
        ],
    },
    {
        id: 'logutils',
        title: 'logutils',
        coordinate: 'io.github.appspiriment.kolt:logutils',
        platforms: 'KMP (commonMain + androidMain + desktopMain + iosMain)',
        summary: 'A single `printLog()` call across every platform, auto-gated on debuggability via AndroidX App Startup — no manual init in production.',
        intro: 'The logging library offers a clean, platform-independent logging API. It removes the necessity of manually initializing third-party loggers or cleaning debug logs from release builds by wrapping Android\'s Log and native output streams. It auto-gates logging in production through AndroidX App Startup based on the app\'s debuggability flag.',
        sections: [
            {
                heading: 'Usage — Any Platform Source Set',
                path: 'libs/logutils/src/commonMain/kotlin/.../Log.kt',
                code: `// 1. Simple logs (defaults to LogLevel.WARN):
"Data synchronization complete".printLog()

// 2. Specify debug tag and custom LogLevel (DEBUG, WARN, INFO, ERROR):
"User profile updated".printLog(tag = "UserProfile", level = LogLevel.DEBUG)

// 3. Log errors and exceptions with stack traces:
try {
    performDangerousAction()
} catch (ex: Exception) {
    "Failed to execute action".printLog(isError = true, throwable = ex)
    // Or call directly on the Throwable:
    ex.printLog(tag = "EngineError")
}`,
            },
            {
                heading: 'Auto-Gating & Manual Init',
                path: 'libs/logutils/src/commonMain/kotlin/.../Log.kt',
                code: `// On Android, logutils registers LogInitializer (AndroidX App Startup) which runs automatically
// before Application.onCreate() and sets:
// Log.enabled = (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0
// This ensures debug logs NEVER print on release builds downloaded from the Play Store.

// Non-Android platforms default to Log.enabled = false.
// You can manually initialize or override logging at your platform entry points:
Log.init(enabled = BuildConfig.DEBUG) // sets Log.enabled directly`,
            },
            {
                heading: 'Disabling Android Startup Initializer',
                path: 'libs/logutils/src/androidMain/AndroidManifest.xml',
                code: `<!-- To turn off LogInitializer and handle logging settings completely manually on Android: -->
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="\${applicationId}.androidx-startup"
    tools:node="merge">
    <meta-data
        android:name="io.github.appspiriment.kolt.logutils.LogInitializer"
        android:value="androidx.startup"
        tools:node="remove" />
</provider>`,
            },
        ],
    },
    {
        id: 'location',
        title: 'location',
        coordinate: 'io.github.appspiriment.kolt:location',
        platforms: 'KMP (commonMain + androidMain + desktopMain + iosMain)',
        summary: 'Geolocation, timezone lookup, and place search behind a common interface with per-platform actuals.',
        intro: 'This geolocation library provides timezone lookup, place search, and coordinate tracking through a common KMP contract. It simplifies requesting permission and fetching location coordinates by exposing a unified platform-agnostic facade. Note that this library is currently speculative and is not yet published as a final artifact.',
        sections: [
            {
                heading: 'CurrentLocationProvider Contract',
                path: 'libs/location/src/commonMain/kotlin/.../CurrentLocationProvider.kt',
                code: `// Interface implemented by platform actuals (e.g. fused location on Android, CoreLocation on iOS):
interface CurrentLocationProvider {
    suspend fun getCurrentLocation(): GeoLocation?
}

// Data holder:
data class GeoLocation(val latitude: Double, val longitude: Double)

// Fetching location in shared logic:
suspend fun fetchUserCoordinates(provider: CurrentLocationProvider) {
    val location = provider.getCurrentLocation()
    if (location != null) {
        "Latitude: \${location.latitude}, Longitude: \${location.longitude}".printLog()
    }
}`,
            },
            {
                heading: 'Timezone & Place Autocomplete Searching',
                path: 'libs/location/src/commonMain/kotlin/.../{TimezoneLookup,PlaceSearch}.kt',
                code: `// 1. Resolve IANA Timezone ID from coordinates:
val timezoneId = TimezoneLookup.forCoordinates(geoLocation) // returns e.g. "Asia/Kolkata"

// 2. Perform location/place autocomplete searches:
suspend fun handlePlaceSearch(query: String) {
    val results: List<PlaceResult> = PlaceSearch.search(query)
    results.forEach { place ->
        println("Found: \${place.displayName} at \${place.location}")
    }
}`,
            },
        ],
    },
    {
        id: 'compose-kmp',
        title: 'compose-kmp',
        coordinate: 'io.github.appspiriment.kolt:compose-kmp',
        platforms: 'KMP Compose Multiplatform (commonMain + androidMain + desktopMain + iosMain)',
        summary: 'The theme system (colors, dimens, typography, fonts, window-size classes) and wrapper types (UiText/UiColor/UiImage/UiDimen) that every component in the catalog builds on.',
        intro: 'The Compose Multiplatform styling library bundles theme tokens and resource wrapper classes. It solves color and typography management issues by providing a standard design system centered around Outfit fonts and G2 curvature continuous shapes. Use this to ensure standard component styling and clean dark/light mode switches across all targets.',
        sections: [
            {
                heading: 'Responsive Screen Breakpoints (WindowInfo)',
                path: 'libs/compose-kmp/src/commonMain/kotlin/.../theme/WindowInfo.kt',
                code: `// Collect responsive screen size metadata inside any Composable:
val windowInfo = rememberWindowInfo()

@Composable
fun MainFeed() {
    val windowInfo = rememberWindowInfo()
    if (windowInfo.screenWidthInfo is WindowInfo.WindowType.Compact) {
        // Render mobile layout
        VerticalFeedList()
    } else {
        // Render tablet / desktop side-by-side grid
        MultiColumnGrid()
    }
}`,
            },
            {
                heading: 'Resource Wrappers Usage',
                path: 'libs/compose-kmp/src/commonMain/kotlin/.../wrappers/{UiText,UiImage,UiColor,UiDimen}.kt',
                code: `// Complete documentation of resource wrappers is available in the Guides section:
// - UiText: Raw/Resource strings with formatting and quantity support.
// - UiImage: Vector graphics, Painter assets, and Coil remote loading.
// - UiColor: Colors, resources, and hex codes (#RRGGBB).
// - UiDimen: Sizes, paddings, and font sizes (sp).
//
// See "Resource Wrappers" under Guides for comprehensive examples.`,
            },
            {
                heading: 'G2 Curvature Continuous Squircles',
                path: 'libs/compose-kmp/src/commonMain/kotlin/.../theme/SmoothCornerShape.kt',
                code: `// Clipper Shape replacing standard sharp circle arcs with continuous curves:
Box(
    modifier = Modifier
        .size(150.dp)
        .clip(SmoothCornerShape(radius = 32.dp, smoothness = 0.55f)) // 0.55f is the squircle sweet spot
        .background(Kolt.colors.primary)
) {
    Text("G2 Curvature Continuous Corners", color = Color.White)
}`,
            },
        ],
    },
    {
        id: 'update-utils',
        title: 'update-utils',
        coordinate: 'io.github.appspiriment.kolt:update-utils',
        platforms: 'Android-only',
        summary: 'Gates forced/optional in-app updates using Firebase Remote Config plus the Google Play In-App Update API.',
        intro: 'The in-app update utility simplifies the integration of the official Google Play In-App Update API with Firebase Remote Config. It prevents architectural clutter by wrapping update-checks in a single clean interface that delegates forced or optional flows. Use this in your Android application modules to force update configurations from the cloud.',
        sections: [
            {
                heading: 'ViewModel Delegate Integration',
                path: 'libs/update-utils/src/main/java/io/github/appspiriment/kolt/updateutils/AppUpdateHelperUtil.kt',
                code: `// Delegate checks directly inside your Hilt ViewModel:
class MainViewModel @Inject constructor(
    // Inject the helper implementation
    private val updateHelper: AppUpdateHelperUtilImpl 
) : ViewModel(), AppUpdateHelperUtil by updateHelper {
    
    init {
        // Triggers the update checks from Play Store and Remote Config
        checkForUpdates() 
    }
}`,
            },
            {
                heading: 'Checking and Gating UI Content',
                path: 'libs/update-utils/src/main/java/io/github/appspiriment/kolt/updateutils/AppUpdateHelperUtil.kt',
                code: `// In your root Android Activity / Composable:
@Composable
fun AppRoot(viewModel: MainViewModel = hiltViewModel()) {
    viewModel.CheckForUpdateAndSetContent(
        content = {
            // Your normal app content when no updates are required
            AppNavHost()
        },
        onForceUpdate = {
            // Custom UI shown when a critical update block is active
            ForceUpdateScreen()
        }
    )
}`,
            },
            {
                heading: 'Firebase Remote Config Keys',
                path: 'Firebase Console',
                code: `// Configure these boolean values in Firebase Remote Config to toggle dialog types:
CRITICAL_UPDATE = true   // Forces a non-dismissable blocker page
FEATURE_DROP    = true   // Displays an optional update prompt with a "Not Now" action`,
            },
        ],
    },
    {
        id: 'compose-utils',
        title: 'compose-utils',
        coordinate: 'io.github.appspiriment.kolt:compose',
        platforms: 'Android-only',
        summary: 'Android-specific Compose components and utility extensions for permissions, photo selectors, and text fields.',
        intro: 'The Android Compose utilities library provides specific lifecycle extensions and wrapper utilities for UI development on the Android platform. It solves the complexity of system-level callbacks such as checking/requesting runtime permissions, invoking photo selectors, and managing system keyboard behaviors.',
        sections: [
            {
                heading: 'Runtime Permission Handlers',
                path: 'libs/compose-utils/src/main/java/io/github/appspiriment/kolt/composeutils/utils/PermissionHandlerUtils.kt',
                code: `// Clean wrapper to request runtime permissions from within the Composable tree:
PermissionHandler.Request(
    permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
    onGranted = {
        // Trigger camera opening
        openCamera()
    },
    onDenied = {
        // Render rationale / disabled states
        showPermissionDeniedMessage()
    }
)`,
            },
            {
                heading: 'System Photo Selector Launcher',
                path: 'libs/compose-utils/src/main/java/io/github/appspiriment/kolt/composeutils/utils/PhotoPickerUtils.kt',
                code: `// Invoke the official Android System Photo Picker (supporting maximum item counts):
val picker = rememberPhotoPickerLauncher { uris ->
    if (uris.isNotEmpty()) {
        "Selected \n\${uris.size} photos".printLog()
        viewModel.uploadPhotos(uris)
    }
}

// Trigger in response to UI clicks:
AppsButton(text = "Select Images".toUiText()) {
    picker.launch(maxItems = 5)
}`,
            },
            {
                heading: 'Soft Keyboard Controller',
                path: 'libs/compose-utils/src/main/java/io/github/appspiriment/kolt/composeutils/utils/DisableSoftKeyboard.kt',
                code: `// Disables the default soft keyboard from showing up when a TextField receives focus.
// Useful when building custom PIN entry pads, calculators, or date pickers:
DisableSoftKeyboard {
    TextField(
        value = pinCodeValue,
        onValueChange = { pinCodeValue = it },
        label = { Text("Enter PIN") }
    )
}`,
            },
            {
                heading: 'System Dialers & Communication Actions',
                path: 'libs/compose-utils/src/main/java/io/github/appspiriment/kolt/composeutils/utils/PhoneActionUtils.kt',
                code: `// Senders that automatically construct dialer intents and email prompts:
context.dialNumber("+123456789")

context.sendEmail(
    to = "support@example.com",
    subject = "App Feedback v1.0",
    body = "Describe your issue here..."
)`,
            },
        ],
    },
];

export function getUtilityById(id) {
    return UTILITIES.find(u => u.id === id);
}
