package com.gabriel.powergabs.presentation.util

import com.gabriel.powergabs.R
import com.gabriel.powergabs.core.result.AppError
import com.gabriel.powergabs.domain.model.Goal
import com.gabriel.powergabs.domain.rule.PrescriptionRules
import com.gabriel.powergabs.presentation.util.Formatters.ElapsedUnit
import com.gabriel.powergabs.testutil.TEST_NOW
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Duration

class FormattersTest {

    @Test
    fun `prescription badge shows ranges with en dash`() {
        val badge = Formatters.prescriptionBadge(PrescriptionRules.forGoal(Goal.HYPERTROPHY))

        assertEquals("3–4 × 8–12 · 60–90 s", badge)
    }

    @Test
    fun `fixed rest is shown as a single number`() {
        val badge = Formatters.prescriptionBadge(PrescriptionRules.forGoal(Goal.CONDITIONING))

        assertEquals("2–3 × 12–15 · 30 s", badge)
    }

    @Test
    fun `strength badge matches the acceptance criterion`() {
        assertEquals("3–5 × 3–6 · 120–180 s", Formatters.prescriptionBadge(PrescriptionRules.forGoal(Goal.STRENGTH)))
    }

    @Test
    fun `elapsed time uses the largest whole unit`() {
        assertEquals(ElapsedUnit.JUST_NOW, Formatters.elapsedSince(TEST_NOW.minusSeconds(30), TEST_NOW).unit)
        assertEquals(Formatters.Elapsed(ElapsedUnit.MINUTES, 5), Formatters.elapsedSince(TEST_NOW.minus(Duration.ofMinutes(5)), TEST_NOW))
        assertEquals(Formatters.Elapsed(ElapsedUnit.HOURS, 3), Formatters.elapsedSince(TEST_NOW.minus(Duration.ofMinutes(200)), TEST_NOW))
        assertEquals(Formatters.Elapsed(ElapsedUnit.DAYS, 2), Formatters.elapsedSince(TEST_NOW.minus(Duration.ofHours(50)), TEST_NOW))
    }

    @Test
    fun `future timestamp is treated as just now`() {
        assertEquals(ElapsedUnit.JUST_NOW, Formatters.elapsedSince(TEST_NOW.plusSeconds(600), TEST_NOW).unit)
    }

    @Test
    fun `each app error has its own message`() {
        assertEquals(R.string.error_network, ErrorMessageMapper.messageFor(AppError.Network))
        assertEquals(R.string.error_http, ErrorMessageMapper.messageFor(AppError.Http(500)))
        assertEquals(R.string.error_serialization, ErrorMessageMapper.messageFor(AppError.Serialization))
        assertEquals(R.string.error_database, ErrorMessageMapper.messageFor(AppError.Database))
        assertEquals(R.string.error_unknown, ErrorMessageMapper.messageFor(AppError.Unknown))
    }
}
