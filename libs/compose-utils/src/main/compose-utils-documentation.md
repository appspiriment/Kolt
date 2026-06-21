# Kolt Compose Utils Library

This document provides detailed documentation for the reusable Jetpack Compose components available in the `compose-utils` library. It is intended for developers and AI agents to understand and implement these components effectively.

## Table of Contents
1.  [Theme & Core](#theme--core)
2.  [Wrappers](#wrappers)
3.  [Containers](#containers)
    -   [PageScaffold](#pagescaffold)
    -   [AppsTopBar](#appstopbar)
    -   [Drawer Scaffolds](#drawer-scaffolds)
    -   [Bottom Navigation](#bottom-navigation)
4.  [Core Components](#core-components)
5.  [Utilities](#utilities)
    -   [Permission Handling](#permission-handling)
    -   [Phone Actions](#phone-actions)

---

## Theme & Core

### `AppspirimentTheme`

The root theme for applying the `Kolt` design system. All composables should be wrapped in this theme.

-   **File Path**: `theme/AppspirimentTheme.kt`
-   **Description**: Applies the custom color scheme, typography, and sizing defined in `Kolt.colors`, `Kolt.typography`, and `Kolt.sizes`.
-   **Usage**:
    ```kotlin
    AppspirimentTheme {
        Surface {
            // Your screen content
        }
    }
    ```

---

## Wrappers

These are wrapper classes to handle resources in a platform-agnostic way, which is especially useful for serialization and testing.

### `UiText`
- **File Path**: `wrappers/UiText.kt`
- **Description**: Represents a piece of text that can be a direct `String` or a string resource `id`. Includes a `toString(context: Context)` method to resolve the text.

### `UiImage`
- **File Path**: `wrappers/UiImage.kt`
- **Description**: Represents an image that can be a drawable resource `id`, a `Bitmap`, or an `ImageVector`.

### `Color`
- **File Path**: `wrappers/Color.kt`
- **Description**: Represents a color that can be a `Color` object or a color resource `id`. Includes an `asColor(context: Context)` method to resolve the color.

### `SerializedColor`
- **File Path**: `utils/SerializedColor.kt`
- A `value class` that wraps `androidx.compose.ui.graphics.Color` to make it serializable using a custom `ColorSerializer`.
- **Usage**:
  ```kotlin
  // To make a color serializable
  val serializableRed = Color.Red.serialized

  // To get the color back
  val color = serializableRed.value
  ```

---

## Containers

### `PageScaffold`

The most basic scaffold for a screen, providing a top bar and loading state management.

-   **File Path**: `components/containers/PageScaffold.kt`
-   **Description**: A wrapper around `androidx.compose.material3.Scaffold` that integrates a loading overlay.
-   **Parameters**:
| Name | Type | Default | Description |
|---|---|---|---|
| `backgroundColor` | `Color` | `Kolt.colors.mainSurface` | The background color of the scaffold. |
| `topBar` | `@Composable () -> Unit` | `{}` | Composable lambda for the top app bar. |
| `bottomBar`| `@Composable () -> Unit` | `{}` | Composable lambda for the bottom navigation bar. |
| `isLoading`| `Boolean` | `false` | If true, shows a loading indicator overlay. |
| `content` | `@Composable (PaddingValues) -> Unit` | | The main content of the screen. |

### `AppsTopBar`

A customizable top app bar with support for navigation icons, titles, and actions.

-   **File Path**: `components/containers/AppsTopBar.kt`
-   **Description**: A customized `TopAppBar` that conforms to the `Kolt` design system.
-   **Parameters**:
| Name | Type | Default | Description |
|---|---|---|---|
| `navMode` | `NavigationMode` | `NavigationMode.EMPTY` | Defines the navigation icon (Back, Drawer, or None). |
| `navIconClick` | `(() -> Unit)?` | `null` | Lambda invoked when the navigation icon is clicked. |
| `appBarTitle` | `AppBarTitle?` | `null` | The title configuration. Can be text, logo, or text with an icon. |
| `background` | `Color` | `Kolt.colors.topAppBar` | The background color. |
| `onTopBarColor` | `Color` | `Kolt.colors.onTopAppBar` | The content color for text and icons. |
| `actions` | `List<AppsTopBarButton>?`| `null` | List of action buttons on the right. |
| `actionsContent`| `@Composable RowScope.(Color) -> Unit` | `{}` | A trailing lambda for custom action content. |

- **Supporting Types**:
  - `NavigationMode`: Enum (`BACK`, `DRAWER`, `EMPTY`) with an associated icon.
  - `AppBarTitle`: Sealed interface (`BrandLogo`, `ScreenTitle`, `ScreenTitleWithIcon`, `None`).
  - `AppsTopBarButton`: Data class for actions (`icon: UiImage`, `onClick: () -> Unit`).
- **Usage Example**:
  ```kotlin
  AppsTopBar(
      navMode = NavigationMode.BACK,
      navIconClick = { /* Handle back press */ },
      appBarTitle = AppBarTitle.ScreenTitle("My Screen".toUiText()),
      actions = listOf(
          AppsTopBarButton(
              icon = Icons.Default.Search.toUiImage(),
              onClick = { /* Handle search */ }
          )
      )
  )
  ```

### Drawer Scaffolds

-   **File Path**: `components/containers/AppsDrawerScaffold.kt`

#### `AppsTitleDrawerScaffold`

A high-level scaffold with a pre-configured `AppsTopBar` and a navigation drawer.

-   **Description**: Ideal for standard screens needing a drawer. It simplifies setup by composing `AppsDrawerScaffold` with a standard top bar and drawer item list.
-   **Parameters**:
| Name | Type | Default | Description |
|---|---|---|---|
| `appBarTitle`| `AppBarTitle?` | `null` | Title for the integrated `AppsTopBar`. |
| `colors` | `ScaffoldColors` | `ScaffoldColors.defaults()` | Color scheme for all scaffold components. |
| `gestureEnabled`| `Boolean`| `true` | Enables swipe gestures for the drawer. |
| `drawerShape`| `Shape`| `RectangleShape` | The shape of the drawer container. |
| `scrimColor`| `Color`| `DrawerDefaults.scrimColor` | Color of the overlay when the drawer is open. |
| `actions`| `List<AppsTopBarButton>?`| `null` | Actions for the integrated `AppsTopBar`. |
| `drawerOptions`| `List<DrawerItem>?`| `null` | List of items to display in the drawer. |
| `drawerHeader`| `(@Composable () -> Unit)?`| `null` | Optional composable for the drawer's header. |
| `drawerFooter`| `(@Composable () -> Unit)?`| `null` | Optional composable for the drawer's footer. |
| `onDrawerItemClicked`| `(DrawerIdentifier) -> Unit`| | Required callback for when a drawer item is clicked. |
| `isLoading`| `Boolean`| `false` | Shows a loading overlay. |
| `content`| `@Composable (PaddingValues) -> Unit`| | The main screen content. |

#### `AppsDrawerScaffold`

A lower-level, flexible scaffold for building a custom navigation drawer experience.

-   **Description**: Gives full control over the `topBar` and `drawerContent` via composable lambdas. Use this for non-standard drawer layouts.
-   **Parameters**: (Includes `colors`, `gestureEnabled`, `isLoading`, `drawerShape`, `scrimColor`, `content` from above, plus:)
| Name | Type | Default | Description |
|---|---|---|---|
| `topBar` | `@Composable (...) -> Unit`| `null` | Lambda to define a custom top bar. |
| `drawerContent`| `@Composable ColumnScope.(...) -> Unit`| `null` | Lambda to define the entire drawer content. |

### Bottom Navigation

-   **File Path**: `components/containers/bottomnavigation/AppsBottomNavigation.kt`

#### `AppsBottomNavigation`

A `NavController`-aware bottom navigation bar with an overflow menu.

-   **Description**: Integrates with Jetpack Navigation, automatically handling selection state and showing a "More" menu if items exceed `visibleItemsCount`.
-   **Parameters**:
| Name | Type | Default | Description |
|---|---|---|---|
| `navController` | `NavController` | | The navigation controller. |
| `bottomBarRoutes`| `List<AppsBottomBarButton>`| | The list of all items for the bar. |
| `visibleItemsCount`| `Int`| `5` | Max visible items before showing a "More" menu. |
| `selectedColor`| `Color`| `Kolt.colors.primary` | Color for selected item's text and icon. |
| `unselectedColor`| `Color`| `Kolt.colors.onPrimaryCardContainer`| Color for unselected items. |
| `selectedBubbleColor`| `Color`| `Color.Transparent` | Background color for the selected item's "bubble". |
| `itemContent` | `@Composable RowScope.(...) -> Unit`| `DefaultBottomNavigationItem` | Lambda to define a custom item UI. |

-   **File Path**: `components/containers/bottomnavigation/AppsBottomNavigationNavHost.kt`

#### `AppsBottomNavigationNavHost`

A scaffold that seamlessly combines a `NavHost` with an `AppsBottomNavigation` bar.

-   **Description**: Reduces boilerplate by automatically showing or hiding the bottom navigation bar based on the current destination.
-   **Parameters**:
| Name | Type | Default | Description |
|---|---|---|---|
| `navController`| `NavHostController`| | The navigation controller. |
| `startDestination`| `Serializable` | | The starting route for the `NavHost`. |
| `bottomBarRoutes`| `List<AppsBottomBarButton>`| | List of top-level routes for the bottom bar. |
| `topDivider`| `@Composable (() -> Unit)?`| `HorizontalDivider(...)` | Composable for the divider above the bar. `null` to hide. |
| `navGraphBuilderScope`| `(NavGraphBuilder) -> Unit`| | Required lambda to define the navigation graph. |
---

## Core Components

### Text: `AppspirimentText`
- **File Path**: `components/core/text/AppspirimentText.kt`
- **Description**: A wrapper around `androidx.compose.material3.Text` that uses a `UiText` object for its `text` parameter and integrates with the `Kolt` theme.

### Images & Icons: `AppsImage`, `AppsIcon`
- **File Path**: `components/core/image/AppsImage.kt`, `components/core/image/AppsIcon.kt`
- **Description**: Wrappers for `Image` and `Icon` that use `UiImage` for their data source and integrate with the theme. `AppsIcon` likely has tinting capabilities.

### Buttons: `AppsIconButton`
- **File Path**: `components/core/buttons/AppsIconButton.kt`
- **Description**: A themed `IconButton` that simplifies creating clickable icons consistent with the design system.

### Spacers: `VerticalSpacer`, `HorizontalSpacer`
- **File Path**: `components/core/VerticalSpacer.kt`
- **Description**: Simple composables to add themed vertical or horizontal space. Takes a `Dp` value, likely defaulting to a theme dimension.

---

## Utilities

### Permission Handling
- **File Path**: `utils/PermissionHandlerUtils.kt`
- **Description**: A set of composables and state management classes to simplify runtime permission requests in Compose.
- **Key Composables**:
  - `rememberAppPermissionsState`: A state holder that manages the status of multiple permissions.
  - `PermissionsManagerContainer`: A container that reacts to permission state (`granted`, `denied`, `checking`).
  - `DefaultPermissionsRequestUI`: A default UI for guiding users through granting permissions or going to settings.

### Phone Actions
- **File Path**: `utils/PhoneActionUtils.kt`
- **Description**: A collection of `remember...` functions that provide a state-based way to trigger common phone intents (call, dial, SMS, WhatsApp, email).
- **Key Composables**:
  - `rememberCallPhoneState`: Triggers a direct phone call (requires permission).
  - `rememberDialPhoneState`: Opens the dialer with a number (no permission needed).
  - `rememberSendSMSState`: Opens the default SMS app.
  - `rememberSendWhatsappState`: Opens a WhatsApp chat.
  - `rememberSendEmailState`: Opens the default email client.
- **Usage**:
  ```kotlin
  val dialerState = rememberDialPhoneState()
  Button(onClick = { dialerState.value = "123-456-7890" }) {
      Text("Dial Number")
  }
  ```
