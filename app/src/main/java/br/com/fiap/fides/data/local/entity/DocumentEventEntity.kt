package br.com.fiap.fides.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "document_events")
data class DocumentEventEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val actorId: String,
    val type: String,
    val occurredAtEpochMillis: Long,
    val details: String?,
)
