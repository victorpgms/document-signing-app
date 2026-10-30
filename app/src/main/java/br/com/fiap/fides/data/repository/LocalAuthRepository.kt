package br.com.fiap.fides.data.repository

import br.com.fiap.fides.core.security.PasswordHasher
import br.com.fiap.fides.data.local.dao.SignerDao
import br.com.fiap.fides.data.local.dao.UserDao
import br.com.fiap.fides.domain.model.UserRole
import br.com.fiap.fides.domain.repository.AuthRepository
import br.com.fiap.fides.domain.repository.AuthenticatedUser

class LocalAuthRepository(
    private val userDao: UserDao,
    private val signerDao: SignerDao,
    private val passwordHasher: PasswordHasher = PasswordHasher(),
) : AuthRepository {
    override suspend fun authenticate(email: String, password: CharArray): AuthenticatedUser? {
        val normalizedEmail = email.trim().lowercase()
        userDao.getByEmail(normalizedEmail)?.let { user ->
            if (!user.active || !passwordHasher.verify(password, user.passwordHash)) return null
            val role = runCatching { UserRole.valueOf(user.role) }.getOrNull() ?: return null
            return AuthenticatedUser(user.id, user.email, user.displayName, role)
        }

        signerDao.getByEmail(normalizedEmail)?.let { signer ->
            if (!signer.active || signer.passwordHash.isBlank() || !passwordHasher.verify(password, signer.passwordHash)) return null
            return AuthenticatedUser(signer.id, signer.email, signer.fullName, UserRole.SIGNER)
        }

        return null
    }
}
