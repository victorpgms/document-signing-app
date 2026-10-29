package br.com.fiap.fides.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordHasherTest {
    private val hasher = PasswordHasher()

    @Test
    fun `hash can be verified with the original password`() {
        val storedHash = hasher.hash("senha-segura".toCharArray())

        assertTrue(hasher.verify("senha-segura".toCharArray(), storedHash))
        assertFalse(hasher.verify("senha-incorreta".toCharArray(), storedHash))
    }

    @Test
    fun `same password receives a different salt`() {
        val firstHash = hasher.hash("senha-segura".toCharArray())
        val secondHash = hasher.hash("senha-segura".toCharArray())

        assertNotEquals(firstHash, secondHash)
    }
}
