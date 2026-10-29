package br.com.fiap.fides.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "signature_records",
    indices = [Index(value = ["documentId"])],
)
data class SignatureRecordEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val signerId: String,
    val fieldId: String,
    val localFilePath: String,
    val createdAtEpochMillis: Long,
)
