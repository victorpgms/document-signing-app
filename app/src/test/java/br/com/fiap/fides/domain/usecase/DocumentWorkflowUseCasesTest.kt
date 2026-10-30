package br.com.fiap.fides.domain.usecase

import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentEvent
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.model.DocumentSigner
import br.com.fiap.fides.domain.model.SignerRole
import br.com.fiap.fides.domain.model.SignatureRecord
import br.com.fiap.fides.domain.repository.DocumentRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant

class DocumentWorkflowUseCasesTest {
    @Test
    fun `draft requires at least one signer`() {
        runBlocking {
            assertThrows(DocumentValidationException::class.java) {
                runBlocking {
                    CreateDocumentDraftUseCase(FakeDocumentRepository())("admin", "Contrato", "contrato.pdf", "/private/contrato.pdf", emptyList())
                }
            }
        }
    }

    @Test
    fun `draft normalizes signer document id`() {
        runBlocking {
            val document = CreateDocumentDraftUseCase(FakeDocumentRepository())(
                ownerId = "admin",
                title = "Contrato",
                originalFileName = "contrato.pdf",
                localFilePath = "/private/contrato.pdf",
                signers = listOf(DocumentSigner("imported-pdf-id", "signer", SignerRole.CONTRACTOR)),
            )

            assertEquals(document.id, document.signers.single().documentId)
        }
    }

    @Test
    fun `sending is idempotent after first status transition`() {
        runBlocking {
            val repository = FakeDocumentRepository()
            val document = Document("doc", "admin", "Contrato", "contrato.pdf", "/private/contrato.pdf", DocumentStatus.DRAFT, Instant.EPOCH, Instant.EPOCH, listOf(DocumentSigner("doc", "signer", SignerRole.CONTRACTOR)))
            repository.document = document
            val useCase = SendDocumentUseCase(repository)

            val first = useCase("doc", "admin")
            val second = useCase("doc", "admin")

            assertEquals(DocumentStatus.SENT, first.status)
            assertEquals(DocumentStatus.SENT, second.status)
            assertEquals(1, repository.sentEvents)
        }
    }
}

private class FakeDocumentRepository : DocumentRepository {
    var document: Document? = null
    var sentEvents = 0
    override suspend fun getAllForUser(userId: String) = listOfNotNull(document)
    override suspend fun getById(id: String) = document
    override suspend fun save(document: Document) { this.document = document }
    override suspend fun delete(id: String) = Unit
    override suspend fun updateStatus(id: String, status: DocumentStatus, updatedAt: Instant) { document = document?.copy(status = status, updatedAt = updatedAt) }
    override suspend fun recordEvent(event: DocumentEvent) { if (event.type.name == "SENT") sentEvents += 1 }
    override suspend fun getEvents(documentId: String) = emptyList<DocumentEvent>()
    override suspend fun markSignerSigned(documentId: String, signerId: String, signedAt: Instant) = Unit
    override suspend fun saveSignature(record: SignatureRecord) = Unit
    override suspend fun getSignatures(documentId: String) = emptyList<SignatureRecord>()
}
