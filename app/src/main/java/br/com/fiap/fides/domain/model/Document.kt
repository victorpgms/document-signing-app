package br.com.fiap.fides.domain.model

import java.time.Instant

data class Document(
    val id: String,
    val ownerId: String,
    val title: String,
    val originalFileName: String,
    val localFilePath: String,
    val status: DocumentStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
    val signers: List<DocumentSigner> = emptyList(),
    val signatureFields: List<SignatureField> = emptyList(),
    val version: Int = 1,
    val previousVersionId: String? = null,
)
