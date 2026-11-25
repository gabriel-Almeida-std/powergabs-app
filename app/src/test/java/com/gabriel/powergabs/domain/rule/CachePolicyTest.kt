package com.gabriel.powergabs.domain.rule

import com.gabriel.powergabs.testutil.TEST_NOW
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

/** RN09 — validade de 24 horas do cache. */
class CachePolicyTest {

    @Test
    fun `cache that was never downloaded is expired`() {
        assertTrue(CachePolicy.isExpired(lastUpdated = null, now = TEST_NOW))
    }

    @Test
    fun `cache updated 23 hours and 59 minutes ago is still valid`() {
        val lastUpdated = TEST_NOW.minus(Duration.ofHours(23).plusMinutes(59))

        assertFalse(CachePolicy.isExpired(lastUpdated, TEST_NOW))
    }

    @Test
    fun `cache updated exactly 24 hours ago is expired`() {
        val lastUpdated = TEST_NOW.minus(Duration.ofHours(24))

        assertTrue(CachePolicy.isExpired(lastUpdated, TEST_NOW))
    }

    @Test
    fun `cache updated more than 24 hours ago is expired`() {
        val lastUpdated = TEST_NOW.minus(Duration.ofDays(3))

        assertTrue(CachePolicy.isExpired(lastUpdated, TEST_NOW))
    }
}
