package com.gabriel.powergabs.domain.rule

import com.gabriel.powergabs.testutil.TEST_ZONE
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.ZonedDateTime

/** RN16 — lembrete diário às 18:00. */
class ReminderRulesTest {

    private fun at(hour: Int, minute: Int = 0) =
        ZonedDateTime.of(2026, 11, 24, hour, minute, 0, 0, TEST_ZONE).toInstant()

    @Test
    fun `before 18h the reminder is scheduled for today`() {
        val delay = ReminderRules.delayUntilNextReminder(at(hour = 10), TEST_ZONE)

        assertEquals(Duration.ofHours(8), delay)
    }

    @Test
    fun `after 18h the reminder is scheduled for tomorrow`() {
        val delay = ReminderRules.delayUntilNextReminder(at(hour = 20), TEST_ZONE)

        assertEquals(Duration.ofHours(22), delay)
    }

    @Test
    fun `exactly at 18h the next reminder is tomorrow`() {
        val delay = ReminderRules.delayUntilNextReminder(at(hour = 18), TEST_ZONE)

        assertEquals(Duration.ofHours(24), delay)
    }
}
