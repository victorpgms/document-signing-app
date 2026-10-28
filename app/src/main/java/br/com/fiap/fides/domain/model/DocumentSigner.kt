package br.com.fiap.fides.domain.model

data class DocumentSigner(
    val documentId: String,
    val signerId: String,
    val role: SignerRole,
    val required: Boolean = true,
)
