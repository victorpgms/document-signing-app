package br.com.fiap.fides.domain.usecase

import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentEvent
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.model.DocumentSigner
import br.com.fiap.fides.domain.model.SignerRole
import br.com.fiap.fides.domain.model.SignatureField
import br.com.fiap.fides.domain.model.SignatureRecord
import br.com.fiap.fides.domain.repository.DocumentRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
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

    @Test
    fun `new version preserves original and resets signing state`() = runBlocking {
        val repository = FakeDocumentRepository()
        repository.document = Document(
            id = "sent-document",
            ownerId = "admin",
            title = "Contrato",
            originalFileName = "contrato.pdf",
            localFilePath = "/private/contrato.pdf",
            status = DocumentStatus.COMPLETED,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            signers = listOf(DocumentSigner("sent-document", "signer", SignerRole.CONTRACTOR, signedAt = Instant.EPOCH)),
            signatureFields = listOf(SignatureField("field", "sent-document", "signer", 0, .1f, .1f, .3f, .1f)),
            version = 2,
            signedFilePath = "/private/signed.pdf",
        )

        val version = CreateNewDocumentVersionUseCase(repository)("sent-document", "admin")

        assertEquals(DocumentStatus.DRAFT, version.status)
        assertEquals(3, version.version)
        assertEquals("sent-document", version.previousVersionId)
        assertEquals("/private/contrato.pdf", version.localFilePath)
        assertNull(version.signedFilePath)
        assertNull(version.signers.single().signedAt)
        assertNotEquals("sent-document", version.id)
        assertEquals(version.id, version.signers.single().documentId)
        assertEquals(version.id, version.signatureFields.single().documentId)
        assertNotEquals("field", version.signatureFields.single().id)
    }

    @Test
    fun `only owner can delete draft`() = runBlocking {
        val repository = FakeDocumentRepository().apply {
            document = Document("draft", "admin", "Rascunho", "a.pdf", "/a.pdf", DocumentStatus.DRAFT, Instant.EPOCH, Instant.EPOCH)
        }

        assertThrows(DocumentValidationException::class.java) {
            runBlocking { DeleteDraftUseCase(repository)("draft", "other-admin") }
        }
        DeleteDraftUseCase(repository)("draft", "admin")
        assertEquals(null, repository.document)
    }

    @Test
    fun `cancel changes pending request and preserves cancellation event`() = runBlocking {
        val repository = FakeDocumentRepository().apply {
            document = Document(
                "sent",
                "admin",
                "Contrato",
                "a.pdf",
                "/a.pdf",
                DocumentStatus.SENT,
                Instant.EPOCH,
                Instant.EPOCH,
                listOf(DocumentSigner("sent", "signer", SignerRole.CONTRACTOR)),
            )
        }

        val cancelled = CancelDocumentUseCase(repository)("sent", "admin")

        assertEquals(DocumentStatus.CANCELLED, cancelled.status)
        assertEquals(DocumentStatus.CANCELLED, repository.document?.status)
        assertEquals(1, repository.cancelledEvents)
    }
}

private class FakeDocumentRepository : DocumentRepository {
    var document: Document? = null
    var sentEvents = 0
    var cancelledEvents = 0
    override suspend fun getAllForUser(userId: String) = listOfNotNull(document)
    override suspend fun getById(id: String) = document
    override suspend fun getByValidationToken(token: String) = document?.takeIf { it.validationToken == token }
    override suspend fun save(document: Document) { this.document = document }
    override suspend fun delete(id: String) { document = null }
    override suspend fun updateStatus(id: String, status: DocumentStatus, updatedAt: Instant) { document = document?.copy(status = status, updatedAt = updatedAt) }
    override suspend fun recordEvent(event: DocumentEvent) {
        if (event.type.name == "SENT") sentEvents += 1
        if (event.type.name == "CANCELLED") cancelledEvents += 1
    }
    override suspend fun getEvents(documentId: String) = emptyList<DocumentEvent>()
    override suspend fun markSignerSigned(documentId: String, signerId: String, signedAt: Instant) = Unit
    override suspend fun saveSignature(record: SignatureRecord) = Unit
    override suspend fun getSignatures(documentId: String) = emptyList<SignatureRecord>()
}
