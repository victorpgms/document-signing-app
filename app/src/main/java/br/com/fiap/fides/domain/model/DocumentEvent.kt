package br.com.fiap.fides.domain.model

import java.time.Instant

enum class DocumentEventType {
    CREATED,
    SENT,
    SIGNED,
    COMPLETED,
}

data class DocumentEvent(
    val id: String,
    val documentId: String,
    val actorId: String,
    val type: DocumentEventType,
    val occurredAt: Instant,
    val details: String? = null,
)
