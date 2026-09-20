package com.expenser.app.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.expenser.app.ExpenserApp

/**
 * Fires for two things: the daily alarm (posts the notification) and BOOT_COMPLETED
 * (alarms are cleared on reboot, so re-arm it from the saved setting).
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val setting = (context.applicationContext as ExpenserApp).container.reminder.value
            if (setting.enabled) ReminderScheduler.schedule(context, setting.hour, setting.minute)
            return
        }
        ReminderNotifier.show(context)
    }
}
