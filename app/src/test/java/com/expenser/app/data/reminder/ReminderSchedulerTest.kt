package com.expenser.app.data.reminder

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class ReminderSchedulerTest {

    @Test
    fun `uses today when the time is still ahead`() {
        val now = LocalDateTime.of(2026, 9, 20, 10, 0)
        assertEquals(LocalDateTime.of(2026, 9, 20, 21, 0), ReminderScheduler.nextTriggerTime(21, 0, now))
    }

    @Test
    fun `rolls to tomorrow when the time has passed`() {
        val now = LocalDateTime.of(2026, 9, 20, 22, 0)
        assertEquals(LocalDateTime.of(2026, 9, 21, 21, 0), ReminderScheduler.nextTriggerTime(21, 0, now))
    }

    @Test
    fun `rolls to tomorrow when the time is exactly now`() {
        val now = LocalDateTime.of(2026, 9, 20, 21, 0)
        assertEquals(LocalDateTime.of(2026, 9, 21, 21, 0), ReminderScheduler.nextTriggerTime(21, 0, now))
    }
}
