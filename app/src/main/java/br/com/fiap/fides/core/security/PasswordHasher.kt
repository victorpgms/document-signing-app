package br.com.fiap.fides.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class PasswordHasher(
    private val secureRandom: SecureRandom = SecureRandom(),
) {
    fun hash(password: CharArray): String {
        require(password.isNotEmpty()) { "Password must not be empty" }
        val salt = ByteArray(SALT_SIZE_BYTES).also(secureRandom::nextBytes)
        val derivedKey = derive(password, salt)
        return "${encode(salt)}.${encode(derivedKey)}"
    }

    fun verify(password: CharArray, storedHash: String): Boolean {
        val parts = storedHash.split('.')
        if (parts.size != 2) return false

        val salt = runCatching { Base64.getDecoder().decode(parts[0]) }.getOrNull() ?: return false
        val expected = runCatching { Base64.getDecoder().decode(parts[1]) }.getOrNull() ?: return false
        val actual = derive(password, salt)
        return MessageDigest.isEqual(expected, actual)
    }

    private fun derive(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun encode(value: ByteArray): String = Base64.getEncoder().withoutPadding().encodeToString(value)

    private companion object {
        const val ALGORITHM = "PBKDF2WithHmacSHA256"
        const val ITERATIONS = 120_000
        const val KEY_LENGTH_BITS = 256
        const val SALT_SIZE_BYTES = 16
    }
}
