package com.gabriel.powergabs.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** RNF03 — a senha nunca é guardada em texto puro. Poucas iterações para o teste ser rápido. */
class PasswordHasherTest {

    private val hasher = PasswordHasher(iterations = 1_000)

    @Test
    fun `correct password is verified`() {
        val salt = hasher.newSalt()
        val stored = hasher.hash("segredo123", salt)

        assertTrue(hasher.verify("segredo123", salt, stored))
    }

    @Test
    fun `wrong password is rejected`() {
        val salt = hasher.newSalt()
        val stored = hasher.hash("segredo123", salt)

        assertFalse(hasher.verify("segredo124", salt, stored))
    }

    @Test
    fun `stored value does not contain the plain password`() {
        val stored = hasher.hash("segredo123", hasher.newSalt())

        assertFalse(stored.contains("segredo123"))
        assertTrue(stored.startsWith("PBKDF2WithHmacSHA256:1000:"))
    }

    @Test
    fun `same password with different salts produces different hashes`() {
        val first = hasher.hash("segredo123", hasher.newSalt())
        val second = hasher.hash("segredo123", hasher.newSalt())

        assertNotEquals(first, second)
    }

    @Test
    fun `same password and salt produce the same hash`() {
        val salt = hasher.newSalt()

        assertEquals(hasher.hash("segredo123", salt), hasher.hash("segredo123", salt))
    }

    @Test
    fun `hash created with other iteration count is still verified`() {
        val salt = hasher.newSalt()
        val stored = PasswordHasher(iterations = 2_000).hash("segredo123", salt)

        assertTrue(hasher.verify("segredo123", salt, stored))
    }

    @Test
    fun `malformed stored value is rejected`() {
        assertFalse(hasher.verify("segredo123", hasher.newSalt(), "not-a-hash"))
    }
}
