package br.com.fiap.fides.domain.model

data class SignatureField(
    val id: String,
    val documentId: String,
    val signerId: String,
    val pageIndex: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val required: Boolean = true,
)
