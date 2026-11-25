package com.gabriel.powergabs.data.auth

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hash de senha com PBKDF2 (RNF03), usando só o `javax.crypto` da plataforma.
 *
 * Por que PBKDF2 e não um SHA-256 simples? Porque ele repete o cálculo milhares de vezes
 * ([iterations]): para quem tenta adivinhar senhas por força bruta, cada tentativa fica cara.
 * O sal aleatório por usuário faz duas senhas iguais gerarem hashes diferentes.
 *
 * O resultado guardado tem o formato `algoritmo:iterações:hashHex`, para que a verificação
 * use sempre os mesmos parâmetros do cadastro. Isso importa porque o `PBKDF2WithHmacSHA256`
 * só existe a partir do Android 8 (API 26); no Android 7 cai para `PBKDF2WithHmacSHA1`.
 */
class PasswordHasher(
    private val iterations: Int = DEFAULT_ITERATIONS,
    private val random: SecureRandom = SecureRandom(),
) {

    fun newSalt(): String = ByteArray(SALT_BYTES).also { random.nextBytes(it) }.toHex()

    fun hash(password: String, saltHex: String): String {
        val algorithm = preferredAlgorithm()
        val hash = derive(password, saltHex.hexToBytes(), algorithm, iterations)
        return listOf(algorithm, iterations.toString(), hash.toHex()).joinToString(SEPARATOR)
    }

    /** Recalcula com os parâmetros guardados e compara em tempo constante (`MessageDigest.isEqual`). */
    fun verify(password: String, saltHex: String, stored: String): Boolean {
        val parts = stored.split(SEPARATOR)
        if (parts.size != 3) return false
        val (algorithm, iterationsText, expectedHex) = parts
        val storedIterations = iterationsText.toIntOrNull() ?: return false
        val actual = try {
            derive(password, saltHex.hexToBytes(), algorithm, storedIterations)
        } catch (e: NoSuchAlgorithmException) {
            return false
        }
        return MessageDigest.isEqual(actual, expectedHex.hexToBytes())
    }

    private fun derive(password: String, salt: ByteArray, algorithm: String, rounds: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, rounds, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded
        } finally {
            spec.clearPassword() // apaga a senha da memória assim que possível
        }
    }

    private fun preferredAlgorithm(): String =
        try {
            SecretKeyFactory.getInstance(SHA256)
            SHA256
        } catch (e: NoSuchAlgorithmException) {
            SHA1
        }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray = chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    companion object {
        const val DEFAULT_ITERATIONS = 120_000
        private const val SALT_BYTES = 16
        private const val KEY_LENGTH_BITS = 256
        private const val SEPARATOR = ":"
        private const val SHA256 = "PBKDF2WithHmacSHA256"
        private const val SHA1 = "PBKDF2WithHmacSHA1"
    }
}
