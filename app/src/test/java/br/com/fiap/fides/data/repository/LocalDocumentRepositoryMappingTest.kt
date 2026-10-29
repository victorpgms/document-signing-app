package br.com.fiap.fides.data.repository

import br.com.fiap.fides.data.local.entity.DocumentEntity
import br.com.fiap.fides.domain.model.DocumentStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalDocumentRepositoryMappingTest {
    @Test
    fun `document entity keeps version metadata`() {
        val entity = DocumentEntity(
            id = "doc-1",
            ownerId = "user-1",
            title = "Contrato",
            originalFileName = "contrato.pdf",
            localFilePath = "/data/user/0/br.com.fiap.fides/files/documents/doc-1.pdf",
            status = DocumentStatus.DRAFT.name,
            createdAtEpochMillis = 1_000L,
            updatedAtEpochMillis = 2_000L,
            version = 2,
            previousVersionId = "doc-0",
        )

        assertEquals(2, entity.version)
        assertEquals("doc-0", entity.previousVersionId)
    }
}
