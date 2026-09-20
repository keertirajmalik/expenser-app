package com.expenser.app.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TIME_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")

/**
 * Stateless daily-reminder editor. Choices are staged in the parent and applied
 * on the profile's Save button, alongside name / theme / currency.
 */
@Composable
fun ReminderSection(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
) {
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }

    // Re-read on every resume so returning from system settings updates the warning below.
    var permissionGranted by remember { mutableStateOf(hasNotificationPermission(context)) }
    LifecycleResumeEffect(Unit) {
        permissionGranted = hasNotificationPermission(context)
        onPauseOrDispose { }
    }

    // The request dialog only appears once; after that launch() returns denied immediately
    // and the switch would simply refuse to move with no explanation.
    var requestDenied by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        requestDenied = !granted
        onEnabledChange(granted)
    }

    // A reminder can be switched on and then have notifications revoked in system
    // settings. The alarm still fires, ReminderNotifier bails, and nothing is shown -
    // so say so rather than claiming "On, every day".
    val blocked = !permissionGranted && (enabled || requestDenied)

    HorizontalDivider(modifier = Modifier.fillMaxWidth())
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            "Daily reminder",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.Start),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Remind me to add transactions", style = MaterialTheme.typography.bodyLarge)
                Text(
                    when {
                        blocked -> "Notifications are turned off for Expenser"
                        enabled -> "On, every day"
                        else -> "Off"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (blocked) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = { on ->
                    if (!on) {
                        requestDenied = false
                        onEnabledChange(false)
                    } else if (permissionGranted) {
                        onEnabledChange(true)
                    } else {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
            )
        }
        OutlinedButton(
            onClick = { showTimePicker = true },
            modifier = Modifier.align(Alignment.Start),
        ) {
            Icon(Icons.Filled.Schedule, contentDescription = null)
            Text("  Reminder time · ${LocalTime.of(hour, minute).format(TIME_LABEL)}")
        }
        if (blocked) {
            OutlinedButton(
                onClick = { context.startActivity(notificationSettingsIntent(context)) },
                modifier = Modifier.align(Alignment.Start),
            ) {
                Icon(Icons.Filled.NotificationsOff, contentDescription = null)
                Text("  Open notification settings")
            }
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = DateFormat.is24HourFormat(context),
            onDismiss = { showTimePicker = false },
            onConfirm = { h, m ->
                onTimeChange(h, m)
                showTimePicker = false
            },
        )
    }
}

private fun hasNotificationPermission(context: android.content.Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

/** The app's own notification settings - the only route back once a request is denied twice. */
private fun notificationSettingsIntent(context: android.content.Context): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour,
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    "Reminder time",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Start),
                )
                TimePicker(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK") }
                }
            }
        }
    }
}
