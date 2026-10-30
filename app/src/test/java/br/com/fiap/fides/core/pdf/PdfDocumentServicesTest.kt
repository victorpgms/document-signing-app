package br.com.fiap.fides.core.pdf

import org.junit.Assert.assertThrows
import org.junit.Test

class PdfDocumentServicesTest {
    @Test
    fun `rejects page outside document bounds`() {
        assertThrows(IllegalArgumentException::class.java) { validatePageIndex(2, 2) }
    }
}
