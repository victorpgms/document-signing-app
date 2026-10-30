package br.com.fiap.fides.domain.usecase

import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentEvent
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.model.DocumentSigner
import br.com.fiap.fides.domain.model.SignatureRecord
import br.com.fiap.fides.domain.model.SignerRole
import br.com.fiap.fides.domain.repository.DocumentRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant

class SignatureWorkflowUseCasesTest {
    @Test
    fun `field cannot leave normalized page bounds`() {
        runBlocking {
            val repository = WorkflowFakeRepository(draft())
            assertThrows(DocumentValidationException::class.java) {
                runBlocking { AddSignatureFieldUseCase(repository)("doc", "signer", 0, .8f, .8f, .3f, .2f) }
            }
        }
    }

    @Test
    fun `review requires a field for every required signer`() {
        runBlocking {
            val repository = WorkflowFakeRepository(draft().copy(signatureFields = emptyList()))
            assertThrows(DocumentValidationException::class.java) {
                runBlocking { ReviewDocumentUseCase(repository)("doc") }
            }
        }
    }

    @Test
    fun `signature is idempotent per field and completion needs all signers`() = runBlocking {
        val field = br.com.fiap.fides.domain.model.SignatureField("field", "doc", "signer", 0, .1f, .1f, .2f, .1f)
        val repository = WorkflowFakeRepository(
            draft().copy(
                status = DocumentStatus.SENT,
                signatureFields = listOf(field),
            ),
        )
        val capture = CaptureSignatureUseCase(repository)
        val first = capture("doc", "signer", "field", "/private/signature.png")
        val second = capture("doc", "signer", "field", "/private/signature.png")
        assertEquals(first.id, second.id)

        val applied = ApplySignatureUseCase(repository)("doc", "signer")
        assertEquals(DocumentStatus.PARTIALLY_SIGNED, applied.status)
        val completed = CompleteDocumentUseCase(repository)("doc", "admin")
        assertEquals(DocumentStatus.COMPLETED, completed.status)
    }

    @Test
    fun `sent document cannot have fields deleted`() {
        runBlocking {
            val field = br.com.fiap.fides.domain.model.SignatureField("field", "doc", "signer", 0, .1f, .1f, .2f, .1f)
            val repository = WorkflowFakeRepository(
                draft().copy(
                    status = DocumentStatus.SENT,
                    signatureFields = listOf(field),
                ),
            )

            assertThrows(DocumentValidationException::class.java) {
                runBlocking { DeleteSignatureFieldUseCase(repository)("doc", "field") }
            }
        }
    }

    @Test
    fun `completion rejects a required signer without signature`() {
        runBlocking {
            val repository = WorkflowFakeRepository(draft().copy(status = DocumentStatus.PARTIALLY_SIGNED))

            assertThrows(DocumentValidationException::class.java) {
                runBlocking { CompleteDocumentUseCase(repository)("doc", "admin") }
            }
        }
    }
}

private class WorkflowFakeRepository(var document: Document) : DocumentRepository {
    private val records = mutableListOf<SignatureRecord>()
    override suspend fun getAllForUser(userId: String) = listOf(document)
    override suspend fun getById(id: String) = document.takeIf { it.id == id }
    override suspend fun save(document: Document) { this.document = document }
    override suspend fun delete(id: String) = Unit
    override suspend fun updateStatus(id: String, status: DocumentStatus, updatedAt: Instant) { document = document.copy(status = status, updatedAt = updatedAt) }
    override suspend fun recordEvent(event: DocumentEvent) = Unit
    override suspend fun getEvents(documentId: String) = emptyList<DocumentEvent>()
    override suspend fun markSignerSigned(documentId: String, signerId: String, signedAt: Instant) {
        document = document.copy(signers = document.signers.map { if (it.signerId == signerId) it.copy(signedAt = signedAt) else it })
    }
    override suspend fun saveSignature(record: SignatureRecord) { records += record }
    override suspend fun getSignatures(documentId: String) = records.filter { it.documentId == documentId }
}

private fun draft() = Document(
    id = "doc",
    ownerId = "admin",
    title = "Contrato",
    originalFileName = "contrato.pdf",
    localFilePath = "/private/contrato.pdf",
    status = DocumentStatus.DRAFT,
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
    signers = listOf(DocumentSigner("doc", "signer", SignerRole.CONTRACTOR)),
)
