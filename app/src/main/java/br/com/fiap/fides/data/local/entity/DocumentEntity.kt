package br.com.fiap.fides.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val ownerId: String,
    val title: String,
    val originalFileName: String,
    val localFilePath: String,
    val status: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val version: Int = 1,
    val previousVersionId: String? = null,
)
