package br.com.fiap.fides.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "document_signers",
    primaryKeys = ["documentId", "signerId"],
)
data class DocumentSignerEntity(
    val documentId: String,
    val signerId: String,
    val role: String,
    val required: Boolean,
)
