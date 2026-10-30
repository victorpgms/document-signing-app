package br.com.fiap.fides.data.repository

import androidx.room.withTransaction
import br.com.fiap.fides.data.local.FidesDatabase
import br.com.fiap.fides.data.local.entity.DocumentEntity
import br.com.fiap.fides.data.local.entity.DocumentEventEntity
import br.com.fiap.fides.data.local.entity.DocumentSignerEntity
import br.com.fiap.fides.data.local.entity.SignatureFieldEntity
import br.com.fiap.fides.data.local.entity.SignatureRecordEntity
import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentEvent
import br.com.fiap.fides.domain.model.DocumentEventType
import br.com.fiap.fides.domain.model.DocumentSigner
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.model.SignatureField
import br.com.fiap.fides.domain.model.SignatureRecord
import br.com.fiap.fides.domain.model.SignerRole
import br.com.fiap.fides.domain.repository.DocumentRepository
import java.time.Instant

class LocalDocumentRepository(private val database: FidesDatabase) : DocumentRepository {
    private val dao = database.documentDao()

    override suspend fun getAllForUser(userId: String): List<Document> {
        val documents = mutableListOf<Document>()
        for (entity in dao.getAllForUser(userId)) {
            documents += entity.toDomain(dao)
        }
        return documents
    }

    override suspend fun getById(id: String): Document? =
        dao.getById(id)?.toDomain(dao)

    override suspend fun save(document: Document) {
        database.withTransaction {
            dao.save(document.toEntity())
            dao.deleteSigners(document.id)
            dao.deleteSignatureFields(document.id)
            dao.saveSigners(document.signers.map { it.toEntity() })
            dao.saveSignatureFields(document.signatureFields.map { it.toEntity() })
        }
    }

    override suspend fun delete(id: String) {
        database.withTransaction {
            dao.deleteSigners(id)
            dao.deleteSignatureFields(id)
            dao.deleteById(id)
        }
    }

    override suspend fun updateStatus(id: String, status: DocumentStatus, updatedAt: Instant) {
        database.documentDao().updateStatus(id, status.name, updatedAt.toEpochMilli())
    }

    override suspend fun recordEvent(event: DocumentEvent) {
        database.documentEventDao().insert(event.toEntity())
    }

    override suspend fun getEvents(documentId: String): List<DocumentEvent> =
        database.documentEventDao().getForDocument(documentId).map { it.toDomain() }

    override suspend fun markSignerSigned(documentId: String, signerId: String, signedAt: Instant) {
        dao.markSignerSigned(documentId, signerId, signedAt.toEpochMilli())
    }

    override suspend fun saveSignature(record: SignatureRecord) {
        database.signatureRecordDao().insert(record.toEntity())
    }

    override suspend fun getSignatures(documentId: String): List<SignatureRecord> =
        database.signatureRecordDao().getForDocument(documentId).map { it.toDomain() }
}

private suspend fun DocumentEntity.toDomain(dao: br.com.fiap.fides.data.local.dao.DocumentDao): Document {
    return Document(
        id = id,
        ownerId = ownerId,
        title = title,
        originalFileName = originalFileName,
        localFilePath = localFilePath,
        status = DocumentStatus.valueOf(status),
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
        signers = dao.getSigners(id).map {
            DocumentSigner(
                it.documentId,
                it.signerId,
                SignerRole.valueOf(it.role),
                it.required,
                it.signedAtEpochMillis?.let(Instant::ofEpochMilli),
            )
        },
        signatureFields = dao.getSignatureFields(id).map {
            SignatureField(it.id, it.documentId, it.signerId, it.pageIndex, it.x, it.y, it.width, it.height, it.required)
        },
        version = version,
        previousVersionId = previousVersionId,
        signedFilePath = signedFilePath,
    )
}

private fun Document.toEntity() = DocumentEntity(
    id = id,
    ownerId = ownerId,
    title = title,
    originalFileName = originalFileName,
    localFilePath = localFilePath,
    status = status.name,
    createdAtEpochMillis = createdAt.toEpochMilli(),
    updatedAtEpochMillis = updatedAt.toEpochMilli(),
    version = version,
    previousVersionId = previousVersionId,
    signedFilePath = signedFilePath,
)

private fun DocumentSigner.toEntity() = DocumentSignerEntity(documentId, signerId, role.name, required, signedAt?.toEpochMilli())

private fun SignatureField.toEntity() = SignatureFieldEntity(id, documentId, signerId, pageIndex, x, y, width, height, required)

private fun DocumentEvent.toEntity() = DocumentEventEntity(id, documentId, actorId, type.name, occurredAt.toEpochMilli(), details)

private fun DocumentEventEntity.toDomain() = DocumentEvent(
    id = id,
    documentId = documentId,
    actorId = actorId,
    type = DocumentEventType.valueOf(type),
    occurredAt = Instant.ofEpochMilli(occurredAtEpochMillis),
    details = details,
)

private fun SignatureRecord.toEntity() = SignatureRecordEntity(
    id = id,
    documentId = documentId,
    signerId = signerId,
    fieldId = fieldId,
    localFilePath = localFilePath,
    createdAtEpochMillis = createdAt.toEpochMilli(),
)

private fun SignatureRecordEntity.toDomain() = SignatureRecord(
    id = id,
    documentId = documentId,
    signerId = signerId,
    fieldId = fieldId,
    localFilePath = localFilePath,
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
)
