package br.com.fiap.fides.domain.repository

import kotlinx.coroutines.flow.Flow
import br.com.fiap.fides.domain.model.UserRole

data class UserSession(
    val userId: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
)

interface SessionRepository {
    val session: Flow<UserSession?>

    suspend fun save(user: AuthenticatedUser)

    suspend fun clear()
}
