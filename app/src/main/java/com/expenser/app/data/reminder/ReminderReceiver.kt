package com.expenser.app.data.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.expenser.app.ExpenserApp

/**
 * Fires for two things: the daily alarm (posts the notification) and BOOT_COMPLETED
 * (alarms are cleared on reboot, so re-arm it from the saved setting).
 *
 * Both actions are matched explicitly. Treating "not BOOT_COMPLETED" as the alarm meant
 * any intent at all posted a notification, which - while the receiver was exported - let
 * any installed app fill the shade on demand by naming this component.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> rearm(context)
            ReminderScheduler.ACTION_SHOW -> ReminderNotifier.show(context)
            else -> Unit // Not an action this receiver owns.
        }
    }

    /** Re-apply the saved reminder after a reboot clears the alarm. */
    private fun rearm(context: Context) {
        val app = context.applicationContext as? ExpenserApp ?: return
        val setting = app.container.reminder.value
        if (setting.enabled) ReminderScheduler.schedule(context, setting.hour, setting.minute)
    }
}
