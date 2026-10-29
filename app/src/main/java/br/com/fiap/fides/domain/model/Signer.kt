package br.com.fiap.fides.domain.model

data class Signer(
    val id: String,
    val fullName: String,
    val email: String,
    val active: Boolean = true,
    val phone: String = "",
    val passwordHash: String = "",
)
