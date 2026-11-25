package com.gabriel.powergabs.domain.rule

/** Motivos de recusa de um campo de credencial (RN15). */
enum class CredentialsError {
    EMAIL_EMPTY,
    EMAIL_INVALID,
    PASSWORD_EMPTY,
    PASSWORD_TOO_SHORT,
    CONFIRMATION_MISMATCH,
}

/** Resultado da validação, com um erro (ou nenhum) por campo, para a tela mostrar ao lado de cada um. */
data class CredentialsValidation(
    val emailError: CredentialsError? = null,
    val passwordError: CredentialsError? = null,
    val confirmationError: CredentialsError? = null,
) {
    val isValid: Boolean get() = emailError == null && passwordError == null && confirmationError == null
}

/** Validação de e-mail e senha (RN15). Kotlin puro: não usa `android.util.Patterns`. */
object CredentialsRules {

    const val MIN_PASSWORD_LENGTH = 6

    private val EMAIL_PATTERN = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /** E-mail é guardado e comparado sem espaços nas pontas e em minúsculas. */
    fun normalizeEmail(email: String): String = email.trim().lowercase()

    fun validateLogin(email: String, password: String): CredentialsValidation =
        CredentialsValidation(
            emailError = emailError(email),
            passwordError = if (password.isEmpty()) CredentialsError.PASSWORD_EMPTY else null,
        )

    fun validateRegistration(email: String, password: String, confirmation: String): CredentialsValidation =
        CredentialsValidation(
            emailError = emailError(email),
            passwordError = when {
                password.isEmpty() -> CredentialsError.PASSWORD_EMPTY
                password.length < MIN_PASSWORD_LENGTH -> CredentialsError.PASSWORD_TOO_SHORT
                else -> null
            },
            confirmationError = if (confirmation != password) CredentialsError.CONFIRMATION_MISMATCH else null,
        )

    private fun emailError(email: String): CredentialsError? {
        val normalized = normalizeEmail(email)
        return when {
            normalized.isEmpty() -> CredentialsError.EMAIL_EMPTY
            !EMAIL_PATTERN.matches(normalized) -> CredentialsError.EMAIL_INVALID
            else -> null
        }
    }
}
