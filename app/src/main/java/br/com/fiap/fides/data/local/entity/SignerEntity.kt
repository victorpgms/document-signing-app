package br.com.fiap.fides.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "signers")
data class SignerEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val active: Boolean,
)
