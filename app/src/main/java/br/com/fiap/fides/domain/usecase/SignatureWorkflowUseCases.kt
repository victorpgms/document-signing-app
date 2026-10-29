package br.com.fiap.fides.domain.usecase

import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentEvent
import br.com.fiap.fides.domain.model.DocumentEventType
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.model.SignatureField
import br.com.fiap.fides.domain.model.SignatureRecord
import br.com.fiap.fides.domain.repository.DocumentRepository
import java.time.Clock
import java.time.Instant
import java.util.UUID

data class DocumentReview(
    val document: Document,
    val requiredSignerCount: Int,
    val fieldCount: Int,
    val message: String,
)

private fun validateBounds(x: Float, y: Float, width: Float, height: Float) {
    if (width <= 0f || height <= 0f) throw DocumentValidationException("O campo deve ter dimensões positivas.")
    if (x < 0f || y < 0f || x + width > 1f || y + height > 1f) {
        throw DocumentValidationException("O campo deve permanecer dentro dos limites da página.")
    }
}

private suspend fun DocumentRepository.requireDraft(documentId: String): Document =
    getById(documentId) ?: throw DocumentValidationException("Documento não encontrado.")

class AddSignatureFieldUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(
        documentId: String,
        signerId: String,
        pageIndex: Int,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        required: Boolean = true,
    ): SignatureField {
        val document = repository.requireDraft(documentId)
        if (document.status != DocumentStatus.DRAFT) throw DocumentValidationException("Campos só podem ser alterados no rascunho.")
        if (pageIndex < 0) throw DocumentValidationException("A página informada é inválida.")
        if (document.signers.none { it.signerId == signerId }) throw DocumentValidationException("O campo precisa estar ligado a um signatário do documento.")
        validateBounds(x, y, width, height)
        val field = SignatureField(UUID.randomUUID().toString(), documentId, signerId, pageIndex, x, y, width, height, required)
        repository.save(document.copy(signatureFields = document.signatureFields + field, updatedAt = Instant.now()))
        return field
    }
}

class MoveSignatureFieldUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(documentId: String, fieldId: String, x: Float, y: Float, width: Float, height: Float): Document {
        val document = repository.requireDraft(documentId)
        if (document.status != DocumentStatus.DRAFT) throw DocumentValidationException("Campos só podem ser alterados no rascunho.")
        validateBounds(x, y, width, height)
        if (document.signatureFields.none { it.id == fieldId }) throw DocumentValidationException("Campo não encontrado.")
        val fields = document.signatureFields.map { field ->
            if (field.id == fieldId) field.copy(x = x, y = y, width = width, height = height) else field
        }
        val updated = document.copy(signatureFields = fields, updatedAt = Instant.now())
        repository.save(updated)
        return updated
    }
}

class DeleteSignatureFieldUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(documentId: String, fieldId: String) {
        val document = repository.requireDraft(documentId)
        if (document.status != DocumentStatus.DRAFT) throw DocumentValidationException("Campos só podem ser alterados no rascunho.")
        if (document.signatureFields.none { it.id == fieldId }) throw DocumentValidationException("Campo não encontrado.")
        repository.save(document.copy(signatureFields = document.signatureFields.filterNot { it.id == fieldId }, updatedAt = Instant.now()))
    }
}

class ReviewDocumentUseCase(private val repository: DocumentRepository) {
    suspend operator fun invoke(documentId: String): DocumentReview {
        val document = repository.requireDraft(documentId)
        if (document.signers.isEmpty()) throw DocumentValidationException("Adicione pelo menos um signatário.")
        if (document.signatureFields.isEmpty()) throw DocumentValidationException("Adicione pelo menos um campo de assinatura.")
        val signerIds = document.signers.map { it.signerId }.toSet()
        if (document.signatureFields.any { it.signerId !in signerIds }) throw DocumentValidationException("Existe um campo sem participante válido.")
        document.signatureFields.forEach { validateBounds(it.x, it.y, it.width, it.height) }
        val requiredWithoutField = document.signers.filter { it.required }.filter { signer ->
            document.signatureFields.none { it.signerId == signer.signerId && it.required }
        }
        if (requiredWithoutField.isNotEmpty()) throw DocumentValidationException("Cada signatário obrigatório precisa de um campo.")
        return DocumentReview(document, document.signers.count { it.required }, document.signatureFields.size, "Documento pronto para revisão e envio.")
    }
}

class CaptureSignatureUseCase(
    private val repository: DocumentRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    suspend operator fun invoke(documentId: String, signerId: String, fieldId: String, localFilePath: String): SignatureRecord {
        if (localFilePath.isBlank()) throw DocumentValidationException("A assinatura capturada é obrigatória.")
        val document = repository.getById(documentId) ?: throw DocumentValidationException("Documento não encontrado.")
        if (document.status !in setOf(DocumentStatus.SENT, DocumentStatus.PARTIALLY_SIGNED)) throw DocumentValidationException("O documento não está disponível para assinatura.")
        val signer = document.signers.firstOrNull { it.signerId == signerId }
            ?: throw DocumentValidationException("O usuário não participa deste documento.")
        val field = document.signatureFields.firstOrNull { it.id == fieldId && it.signerId == signer.signerId }
            ?: throw DocumentValidationException("Este campo não pertence ao signatário.")
        repository.getSignatures(documentId).firstOrNull { it.fieldId == field.id }?.let { return it }
        val record = SignatureRecord(UUID.randomUUID().toString(), documentId, signerId, field.id, localFilePath, Instant.now(clock))
        repository.saveSignature(record)
        return record
    }
}

class ApplySignatureUseCase(
    private val repository: DocumentRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    suspend operator fun invoke(documentId: String, signerId: String): Document {
        val document = repository.getById(documentId) ?: throw DocumentValidationException("Documento não encontrado.")
        val signer = document.signers.firstOrNull { it.signerId == signerId }
            ?: throw DocumentValidationException("O usuário não participa deste documento.")
        val signerFields = document.signatureFields.filter { it.signerId == signerId && it.required }
        val signatures = repository.getSignatures(documentId).filter { it.signerId == signerId }.map { it.fieldId }.toSet()
        if (signerFields.any { it.id !in signatures }) throw DocumentValidationException("Todos os campos obrigatórios devem receber assinatura.")
        val now = Instant.now(clock)
        if (signer.signedAt == null) {
            repository.markSignerSigned(documentId, signerId, now)
            repository.recordEvent(DocumentEvent(UUID.randomUUID().toString(), documentId, signerId, DocumentEventType.SIGNED, now))
        }
        val nextStatus = if (document.status == DocumentStatus.SENT) DocumentStatus.PARTIALLY_SIGNED else document.status
        if (document.status != nextStatus) repository.updateStatus(documentId, nextStatus, now)
        return document.copy(status = nextStatus, updatedAt = now, signers = document.signers.map { if (it.signerId == signerId) it.copy(signedAt = now) else it })
    }
}

class CompleteDocumentUseCase(
    private val repository: DocumentRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    suspend operator fun invoke(documentId: String, actorId: String): Document {
        val document = repository.getById(documentId) ?: throw DocumentValidationException("Documento não encontrado.")
        if (document.signers.none { it.required }) throw DocumentValidationException("O documento não possui signatários obrigatórios.")
        if (document.signers.any { it.required && it.signedAt == null }) throw DocumentValidationException("Ainda existem assinaturas obrigatórias pendentes.")
        if (document.status == DocumentStatus.COMPLETED) return document
        val now = Instant.now(clock)
        repository.updateStatus(documentId, DocumentStatus.COMPLETED, now)
        repository.recordEvent(DocumentEvent(UUID.randomUUID().toString(), documentId, actorId, DocumentEventType.COMPLETED, now))
        return document.copy(status = DocumentStatus.COMPLETED, updatedAt = now)
    }
}
