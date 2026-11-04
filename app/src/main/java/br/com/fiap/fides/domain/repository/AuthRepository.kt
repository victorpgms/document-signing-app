package br.com.fiap.fides.domain.repository

import br.com.fiap.fides.domain.model.UserRole

data class AuthenticatedUser(
    val id: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
)

data class RegistrationResult(
    val user: AuthenticatedUser? = null,
    val error: String? = null,
)

interface AuthRepository {
    suspend fun authenticate(email: String, password: CharArray): AuthenticatedUser?

    suspend fun register(
        displayName: String,
        email: String,
        password: CharArray,
        role: UserRole,
        phone: String = "",
    ): RegistrationResult
}
