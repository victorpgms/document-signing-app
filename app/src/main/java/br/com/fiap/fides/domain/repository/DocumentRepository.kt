package br.com.fiap.fides.domain.repository

import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentEvent
import br.com.fiap.fides.domain.model.DocumentStatus
import java.time.Instant

interface DocumentRepository {
    suspend fun getAllForUser(userId: String): List<Document>

    suspend fun getById(id: String): Document?

    suspend fun save(document: Document)

    suspend fun delete(id: String)

    suspend fun updateStatus(id: String, status: DocumentStatus, updatedAt: Instant)

    suspend fun recordEvent(event: DocumentEvent)

    suspend fun getEvents(documentId: String): List<DocumentEvent>
}
