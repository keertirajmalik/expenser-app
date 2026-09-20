package com.expenser.app.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDateTime
import java.time.ZoneId

/** Persisted daily-reminder preference. */
data class ReminderSetting(val enabled: Boolean, val hour: Int, val minute: Int)

/**
 * Schedules a repeating daily "add your transactions" reminder with the platform
 * [AlarmManager]. Inexact repeating avoids the exact-alarm permission and still
 * fires roughly at the chosen time, which is fine for an end-of-day nudge. The
 * alarm is cleared on reboot, so [ReminderReceiver] reschedules on boot.
 */
object ReminderScheduler {

    private const val REQUEST_CODE = 4201
    const val ACTION_SHOW = "com.expenser.app.action.SHOW_REMINDER"

    fun schedule(context: Context, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val triggerAt = nextTriggerTime(hour, minute)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            pendingIntent(context),
        )
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(pendingIntent(context))
    }

    /** The next date-time at [hour]:[minute]; today if still ahead, otherwise tomorrow. */
    fun nextTriggerTime(hour: Int, minute: Int, now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        val today = now.toLocalDate().atTime(hour, minute)
        return if (today.isAfter(now)) today else today.plusDays(1)
    }

    private fun pendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).setAction(ACTION_SHOW)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
