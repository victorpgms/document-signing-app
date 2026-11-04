package br.com.fiap.fides.domain.repository

import br.com.fiap.fides.domain.model.UserRole

data class AuthenticatedUser(
    val id: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val phone: String = "",
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

    suspend fun getProfile(userId: String): AuthenticatedUser?

    suspend fun updateProfile(
        userId: String,
        displayName: String,
        email: String,
        phone: String,
    ): RegistrationResult

    suspend fun updatePassword(userId: String, currentPassword: CharArray, newPassword: CharArray): String?
}
