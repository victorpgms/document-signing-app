package br.com.fiap.fides.core.pdf

import org.junit.Assert.assertEquals
import org.junit.Test

class PdfImportServiceTest {
    @Test
    fun `sanitizes path separators and unsupported characters`() {
        assertEquals("contrato_final.pdf", sanitizePdfFileName("../contrato:final.pdf"))
    }

    @Test
    fun `uses fallback when display name is blank`() {
        assertEquals("documento.pdf", sanitizePdfFileName(" / "))
    }
}
