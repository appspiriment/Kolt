package io.github.appspiriment.kolt.composeutils.components.containers.types

import io.github.appspiriment.kolt.composekmp.wrappers.UiImage

/**
 * A data class representing an item in the bottom navigation bar.
 *
 * @param name The text label to display for the item.
 * @param route The destination route from your NavGraph (typically a serializable object).
 * @param icon The icon to display for the item.
 */
data class AppsBottomBarButton(val name: String, val route: Any, val icon: UiImage)
