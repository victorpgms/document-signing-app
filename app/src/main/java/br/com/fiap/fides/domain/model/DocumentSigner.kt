package br.com.fiap.fides.domain.model

import java.time.Instant

data class DocumentSigner(
    val documentId: String,
    val signerId: String,
    val role: SignerRole,
    val required: Boolean = true,
    val signedAt: Instant? = null,
)
