package br.com.fiap.fides.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signature_fields")
data class SignatureFieldEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val signerId: String,
    val pageIndex: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val required: Boolean,
)
