package com.expenser.app.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expenser.app.ExpenserApp
import com.expenser.app.data.model.ThemeMode
import com.expenser.app.ui.common.Avatar
import com.expenser.app.ui.common.BentoCard
import com.expenser.app.ui.common.RowDivider
import com.expenser.app.ui.common.SUPPORTED_CURRENCIES
import com.expenser.app.ui.common.SectionLabel
import com.expenser.app.ui.common.SegmentedControl
import com.expenser.app.ui.common.copyImageToInternal
import com.expenser.app.ui.common.currencyLabel
import com.expenser.app.ui.common.filledFieldColors
import com.expenser.app.ui.common.linkColor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory),
) {
    val context = LocalContext.current
    // Reminder settings aren't part of this refactor - still owned by AppContainer directly.
    val container = (context.applicationContext as ExpenserApp).container
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Re-seed local editing state when the user row first loads.
    var name by remember(user?.id) { mutableStateOf(user?.name ?: "") }
    var imagePath by remember(user?.id) { mutableStateOf(user?.image) }

    // Live theme preview: tapping a chip previews it app-wide; the saved theme is
    // restored when leaving this screen unless Save was pressed. The preview lives on
    // AppContainer (app-wide), so it must be cleared on dispose, not on the ViewModel's
    // onCleared() - that only fires once the nav backstack entry is destroyed (e.g. on
    // back-press), not when navigating forward to Categories while the entry survives.
    val previewTheme by viewModel.previewTheme.collectAsStateWithLifecycle()
    val currentTheme = previewTheme ?: themeMode
    DisposableEffect(Unit) {
        onDispose { viewModel.previewTheme(null) }
    }
    // Pending currency choice; applied on Save (no live preview needed here).
    val savedCurrency by viewModel.currency.collectAsStateWithLifecycle()
    var selectedCurrency by remember { mutableStateOf(savedCurrency) }

    // Pending daily-reminder choice; applied on Save alongside the rest.
    val reminder by container.reminder.collectAsStateWithLifecycle()
    var reminderEnabled by remember(reminder) { mutableStateOf(reminder.enabled) }
    var reminderHour by remember(reminder) { mutableStateOf(reminder.hour) }
    var reminderMinute by remember(reminder) { mutableStateOf(reminder.minute) }

    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) scope.launch {
            imagePath = copyImageToInternal(context, uri) ?: imagePath
        }
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    val save = {
        viewModel.save(name, imagePath, currentTheme, selectedCurrency)
        container.setReminder(reminderEnabled, reminderHour, reminderMinute)
    }
    val pickPhoto = {
        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = save,
                        enabled = name.isNotBlank(),
                        colors = ButtonDefaults.textButtonColors(contentColor = linkColor()),
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // One tap target for the photo; the camera badge overhangs the circle.
                Box(
                    modifier = Modifier.clickable(onClickLabel = "Change photo", onClick = pickPhoto),
                ) {
                    Avatar(name = name, imagePath = imagePath, size = 96.dp)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                Text(
                    name.ifBlank { "Your name" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp),
                )
                user?.username?.let {
                    Text(
                        "@$it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            SectionLabel("Account")
            BentoCard {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = filledFieldColors(MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                )
                RowDivider()
                SettingsRow(
                    title = "Username",
                    subtitle = "Can't be changed",
                    value = user?.username ?: "",
                )
            }

            SectionLabel("Preferences", modifier = Modifier.padding(top = 12.dp))
            BentoCard {
                Box {
                    var currencyMenu by remember { mutableStateOf(false) }
                    SettingsRow(
                        title = "Currency",
                        value = currencyLabel(selectedCurrency),
                        onClick = { currencyMenu = true },
                    )
                    DropdownMenu(
                        expanded = currencyMenu,
                        onDismissRequest = { currencyMenu = false },
                        modifier = Modifier.align(Alignment.TopEnd),
                    ) {
                        SUPPORTED_CURRENCIES.forEach { code ->
                            DropdownMenuItem(
                                text = { Text(currencyLabel(code)) },
                                onClick = {
                                    selectedCurrency = code
                                    currencyMenu = false
                                },
                            )
                        }
                    }
                }
                RowDivider()
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Theme", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    SegmentedControl(
                        options = ThemeMode.entries,
                        selected = currentTheme,
                        onSelect = { viewModel.previewTheme(it) },
                        label = { it.name },
                        leading = { Icon(themeIcon(it), contentDescription = null, modifier = Modifier.size(16.dp)) },
                    )
                }
            }

            SectionLabel("Daily reminder", modifier = Modifier.padding(top = 12.dp))
            ReminderSection(
                enabled = reminderEnabled,
                hour = reminderHour,
                minute = reminderMinute,
                onEnabledChange = { reminderEnabled = it },
                onTimeChange = { h, m -> reminderHour = h; reminderMinute = m },
            )

            SectionLabel("Your data", modifier = Modifier.padding(top = 12.dp))
            BackupSection(snackbarHostState = snackbarHostState)
        }
    }
}

private fun themeIcon(mode: ThemeMode) = when (mode) {
    ThemeMode.System -> Icons.Filled.BrightnessAuto
    ThemeMode.Light -> Icons.Filled.LightMode
    ThemeMode.Dark -> Icons.Filled.DarkMode
}
