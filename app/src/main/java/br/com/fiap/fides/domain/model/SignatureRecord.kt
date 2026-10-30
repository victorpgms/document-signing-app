package br.com.fiap.fides.domain.model

import java.time.Instant

data class SignatureRecord(
    val id: String,
    val documentId: String,
    val signerId: String,
    val fieldId: String,
    val localFilePath: String,
    val createdAt: Instant,
)
