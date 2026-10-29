package br.com.fiap.fides.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class SignerTest {
    @Test
    fun `signer defaults to active`() {
        val signer = Signer("id", "Maria Silva", "maria@example.com")

        assertEquals(true, signer.active)
    }
}
