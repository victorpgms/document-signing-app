package br.com.fiap.fides.domain.repository

import br.com.fiap.fides.domain.model.UserRole

data class AuthenticatedUser(
    val id: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
)

interface AuthRepository {
    suspend fun authenticate(email: String, password: CharArray): AuthenticatedUser?
}
