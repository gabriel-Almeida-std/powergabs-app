package com.gabriel.powergabs.domain.rule

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** RN15 — validação de credenciais. */
class CredentialsRulesTest {

    @Test
    fun `valid registration has no errors`() {
        val validation = CredentialsRules.validateRegistration("gabriel@mail.com", "123456", "123456")

        assertTrue(validation.isValid)
    }

    @Test
    fun `empty email is rejected`() {
        val validation = CredentialsRules.validateLogin("   ", "123456")

        assertEquals(CredentialsError.EMAIL_EMPTY, validation.emailError)
    }

    @Test
    fun `email without domain is rejected`() {
        val validation = CredentialsRules.validateLogin("gabriel@", "123456")

        assertEquals(CredentialsError.EMAIL_INVALID, validation.emailError)
    }

    @Test
    fun `email with surrounding spaces and uppercase is accepted after normalization`() {
        val validation = CredentialsRules.validateLogin("  Gabriel@Mail.COM ", "123456")

        assertNull(validation.emailError)
        assertEquals("gabriel@mail.com", CredentialsRules.normalizeEmail("  Gabriel@Mail.COM "))
    }

    @Test
    fun `password shorter than 6 characters is rejected on registration`() {
        val validation = CredentialsRules.validateRegistration("gabriel@mail.com", "12345", "12345")

        assertEquals(CredentialsError.PASSWORD_TOO_SHORT, validation.passwordError)
        assertFalse(validation.isValid)
    }

    @Test
    fun `confirmation different from password is rejected`() {
        val validation = CredentialsRules.validateRegistration("gabriel@mail.com", "123456", "654321")

        assertEquals(CredentialsError.CONFIRMATION_MISMATCH, validation.confirmationError)
    }

    @Test
    fun `empty password is rejected on login`() {
        val validation = CredentialsRules.validateLogin("gabriel@mail.com", "")

        assertEquals(CredentialsError.PASSWORD_EMPTY, validation.passwordError)
    }
}
