package br.com.fiap.fides.domain.repository

import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentEvent
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.model.SignatureRecord
import java.time.Instant

interface DocumentRepository {
    suspend fun getAllForUser(userId: String): List<Document>

    suspend fun getById(id: String): Document?

    suspend fun save(document: Document)

    suspend fun delete(id: String)

    suspend fun updateStatus(id: String, status: DocumentStatus, updatedAt: Instant)

    suspend fun recordEvent(event: DocumentEvent)

    suspend fun getEvents(documentId: String): List<DocumentEvent>

    suspend fun markSignerSigned(documentId: String, signerId: String, signedAt: Instant)

    suspend fun saveSignature(record: SignatureRecord)

    suspend fun getSignatures(documentId: String): List<SignatureRecord>
}
