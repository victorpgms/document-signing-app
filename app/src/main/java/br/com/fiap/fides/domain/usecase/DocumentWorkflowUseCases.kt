package br.com.fiap.fides.domain.usecase

import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentEvent
import br.com.fiap.fides.domain.model.DocumentEventType
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.model.DocumentSigner
import br.com.fiap.fides.domain.repository.DocumentRepository
import java.time.Clock
import java.time.Instant
import java.util.UUID

class DocumentValidationException(message: String) : IllegalArgumentException(message)

class CreateDocumentDraftUseCase(
    private val repository: DocumentRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    suspend operator fun invoke(
        ownerId: String,
        title: String,
        originalFileName: String,
        localFilePath: String,
        signers: List<DocumentSigner>,
    ): Document {
        if (ownerId.isBlank()) throw DocumentValidationException("O proprietário é obrigatório.")
        if (title.isBlank()) throw DocumentValidationException("O título é obrigatório.")
        if (originalFileName.isBlank() || localFilePath.isBlank()) throw DocumentValidationException("O PDF importado é obrigatório.")
        if (signers.isEmpty()) throw DocumentValidationException("Selecione pelo menos um signatário.")
        if (signers.any { it.required && it.signerId.isBlank() }) throw DocumentValidationException("Todo participante deve ter um signatário válido.")
        if (signers.map { it.signerId }.toSet().size != signers.size) throw DocumentValidationException("Não repita o mesmo signatário no documento.")

        val now = Instant.now(clock)
        val documentId = UUID.randomUUID().toString()
        val normalizedSigners = signers.map { it.copy(documentId = documentId) }
        return Document(
            id = documentId,
            ownerId = ownerId,
            title = title.trim(),
            originalFileName = originalFileName,
            localFilePath = localFilePath,
            status = DocumentStatus.DRAFT,
            createdAt = now,
            updatedAt = now,
            signers = normalizedSigners,
        ).also { document ->
            repository.save(document)
            repository.recordEvent(DocumentEvent(UUID.randomUUID().toString(), document.id, ownerId, DocumentEventType.CREATED, now))
        }
    }
}

class SendDocumentUseCase(
    private val repository: DocumentRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    suspend operator fun invoke(documentId: String, actorId: String): Document {
        val document = repository.getById(documentId) ?: throw DocumentValidationException("Documento não encontrado.")
        if (document.status != DocumentStatus.DRAFT) return document
        if (document.signers.none { it.required }) throw DocumentValidationException("O envio exige um signatário obrigatório.")
        val now = Instant.now(clock)
        repository.updateStatus(document.id, DocumentStatus.SENT, now)
        repository.recordEvent(DocumentEvent(UUID.randomUUID().toString(), document.id, actorId, DocumentEventType.SENT, now))
        return document.copy(status = DocumentStatus.SENT, updatedAt = now)
    }
}

class TrackDocumentsUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(userId: String): List<Document> = repository.getAllForUser(userId)
}
