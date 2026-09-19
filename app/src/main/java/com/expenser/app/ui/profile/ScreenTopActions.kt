package com.expenser.app.ui.profile

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

/** Standard top-app-bar actions on the main screens: Categories, then the profile avatar. */
@Composable
fun RowScope.ScreenTopActions(
    onCategoriesClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    IconButton(onClick = onCategoriesClick) {
        Icon(Icons.Filled.Category, contentDescription = "Categories")
    }
    ProfileAvatarAction(onClick = onProfileClick)
}
