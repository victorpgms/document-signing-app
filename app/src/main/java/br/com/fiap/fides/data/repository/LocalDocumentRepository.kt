package br.com.fiap.fides.data.repository

import androidx.room.withTransaction
import br.com.fiap.fides.data.local.FidesDatabase
import br.com.fiap.fides.data.local.entity.DocumentEntity
import br.com.fiap.fides.data.local.entity.DocumentSignerEntity
import br.com.fiap.fides.data.local.entity.SignatureFieldEntity
import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentSigner
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.model.SignatureField
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
        signers = dao.getSigners(id).map { DocumentSigner(it.documentId, it.signerId, SignerRole.valueOf(it.role), it.required) },
        signatureFields = dao.getSignatureFields(id).map {
            SignatureField(it.id, it.documentId, it.signerId, it.pageIndex, it.x, it.y, it.width, it.height, it.required)
        },
        version = version,
        previousVersionId = previousVersionId,
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
)

private fun DocumentSigner.toEntity() = DocumentSignerEntity(documentId, signerId, role.name, required)

private fun SignatureField.toEntity() = SignatureFieldEntity(id, documentId, signerId, pageIndex, x, y, width, height, required)
