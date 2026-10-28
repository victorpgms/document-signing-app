package br.com.fiap.fides.domain.repository

import br.com.fiap.fides.domain.model.Document

interface DocumentRepository {
    suspend fun getAllForUser(userId: String): List<Document>

    suspend fun getById(id: String): Document?

    suspend fun save(document: Document)

    suspend fun delete(id: String)
}
