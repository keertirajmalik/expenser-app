package com.expenser.app.ui.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.ui.common.Avatar

/** Tappable avatar for a top app bar's actions slot; opens the profile screen. */
@Composable
fun ProfileAvatarAction(
    onClick: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    Avatar(
        name = user?.name ?: "",
        imagePath = user?.image,
        size = 32.dp,
        modifier = Modifier
            .padding(end = 12.dp)
            .clickable(onClick = onClick),
    )
}
