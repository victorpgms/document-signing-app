package br.com.fiap.fides.data.repository

import br.com.fiap.fides.core.security.PasswordHasher
import br.com.fiap.fides.data.local.dao.UserDao
import br.com.fiap.fides.domain.model.UserRole
import br.com.fiap.fides.domain.repository.AuthRepository
import br.com.fiap.fides.domain.repository.AuthenticatedUser

class LocalAuthRepository(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher = PasswordHasher(),
) : AuthRepository {
    override suspend fun authenticate(email: String, password: CharArray): AuthenticatedUser? {
        val user = userDao.getByEmail(email.trim().lowercase()) ?: return null
        if (!user.active || !passwordHasher.verify(password, user.passwordHash)) return null

        val role = runCatching { UserRole.valueOf(user.role) }.getOrNull() ?: return null
        return AuthenticatedUser(
            id = user.id,
            email = user.email,
            displayName = user.displayName,
            role = role,
        )
    }
}
