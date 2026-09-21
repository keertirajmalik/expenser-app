package com.expenser.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.expenser.app.ExpenserApp
import com.expenser.app.ui.common.Avatar

/**
 * Tappable avatar for a top app bar's actions slot; opens the profile screen.
 *
 * Reads the shared user flow off [com.expenser.app.di.AppContainer] rather than taking a
 * ProfileViewModel. This composable appears in every main screen's top bar, and
 * `viewModel()` resolves against the current NavBackStackEntry inside a NavHost, so a
 * ViewModel here meant one per tab - each collecting the same row, each carrying the
 * profile screen's theme-preview and currency-commit surface, to draw a 32dp circle.
 */
@Composable
fun ProfileAvatarAction(onClick: () -> Unit) {
    val container = (LocalContext.current.applicationContext as ExpenserApp).container
    val user by container.user.collectAsStateWithLifecycle()
    Avatar(
        name = user?.name ?: "",
        imagePath = user?.image,
        size = 32.dp,
        modifier = Modifier
            .padding(end = 12.dp)
            .clickable(onClick = onClick),
    )
}
