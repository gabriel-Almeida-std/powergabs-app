package com.gabriel.powergabs.data.notification

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.gabriel.powergabs.testutil.FixedTimeProvider
import com.gabriel.powergabs.testutil.TEST_ZONE
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/** RF17, RN16 — CA24. */
class ReminderSchedulerTest {

    private val workManager = mockk<WorkManager>(relaxed = true)
    private val tenAm = ZonedDateTime.of(2026, 11, 24, 10, 0, 0, 0, TEST_ZONE).toInstant()

    @Test
    fun `reminder is scheduled daily with KEEP and delay until 18h`() {
        // Arrange
        val request = slot<PeriodicWorkRequest>()
        every { workManager.enqueueUniquePeriodicWork(any(), any(), capture(request)) } returns mockk()
        val scheduler = ReminderScheduler(workManager, FixedTimeProvider(instant = tenAm))

        // Act
        scheduler.schedule()

        // Assert
        verify { workManager.enqueueUniquePeriodicWork(ReminderScheduler.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, any()) }
        assertEquals(Duration.ofHours(8).toMillis(), request.captured.workSpec.initialDelay)
        assertEquals(TimeUnit.DAYS.toMillis(1), request.captured.workSpec.intervalDuration)
    }
}
