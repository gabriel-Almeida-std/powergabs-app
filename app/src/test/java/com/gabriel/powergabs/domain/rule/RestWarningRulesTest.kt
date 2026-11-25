package com.gabriel.powergabs.domain.rule

import com.gabriel.powergabs.testutil.TEST_ZONE
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZonedDateTime

/** RN10 — aviso de descanso quando o grupo foi treinado no dia anterior. */
class RestWarningRulesTest {

    // 24/11/2026 às 08:00 em Manaus
    private val now = ZonedDateTime.of(2026, 11, 24, 8, 0, 0, 0, TEST_ZONE).toInstant()

    private fun at(day: Int, hour: Int, minute: Int = 0) =
        ZonedDateTime.of(2026, 11, day, hour, minute, 0, 0, TEST_ZONE).toInstant()

    @Test
    fun `exercise done yesterday triggers the rest warning`() {
        val doneDates = listOf(at(day = 23, hour = 19))

        assertTrue(RestWarningRules.shouldWarn(doneDates, now, TEST_ZONE))
    }

    @Test
    fun `exercise done late last night still counts as yesterday`() {
        val doneDates = listOf(at(day = 23, hour = 23, minute = 59))

        assertTrue(RestWarningRules.shouldWarn(doneDates, now, TEST_ZONE))
    }

    @Test
    fun `exercise done today does not trigger the warning`() {
        val doneDates = listOf(at(day = 24, hour = 0, minute = 1))

        assertFalse(RestWarningRules.shouldWarn(doneDates, now, TEST_ZONE))
    }

    @Test
    fun `exercise done two days ago does not trigger the warning`() {
        val doneDates = listOf(at(day = 22, hour = 20))

        assertFalse(RestWarningRules.shouldWarn(doneDates, now, TEST_ZONE))
    }

    @Test
    fun `no done exercises means no warning`() {
        assertFalse(RestWarningRules.shouldWarn(emptyList(), now, TEST_ZONE))
    }
}
