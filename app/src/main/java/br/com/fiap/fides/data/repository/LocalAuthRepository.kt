package br.com.fiap.fides.data.repository

import br.com.fiap.fides.core.security.PasswordHasher
import br.com.fiap.fides.data.local.dao.SignerDao
import br.com.fiap.fides.data.local.dao.UserDao
import br.com.fiap.fides.data.local.entity.SignerEntity
import br.com.fiap.fides.data.local.entity.UserEntity
import br.com.fiap.fides.domain.model.UserRole
import br.com.fiap.fides.domain.repository.AuthRepository
import br.com.fiap.fides.domain.repository.AuthenticatedUser
import br.com.fiap.fides.domain.repository.RegistrationResult
import java.util.UUID

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

    override suspend fun register(
        displayName: String,
        email: String,
        password: CharArray,
        role: UserRole,
        phone: String,
    ): RegistrationResult {
        val normalizedName = displayName.trim()
        val normalizedEmail = email.trim().lowercase()
        if (normalizedName.isBlank()) return RegistrationResult(error = "Informe seu nome.")
        if (!normalizedEmail.contains("@") || normalizedEmail.endsWith("@")) {
            return RegistrationResult(error = "Informe um e-mail válido.")
        }
        if (password.size < 6) return RegistrationResult(error = "A senha deve ter pelo menos 6 caracteres.")
        if (userDao.getByEmail(normalizedEmail) != null || signerDao.getByEmail(normalizedEmail) != null) {
            return RegistrationResult(error = "Este e-mail já está cadastrado.")
        }

        val id = UUID.randomUUID().toString()
        val passwordHash = passwordHasher.hash(password)
        when (role) {
            UserRole.ADMIN -> userDao.insert(
                UserEntity(
                    id = id,
                    email = normalizedEmail,
                    displayName = normalizedName,
                    passwordHash = passwordHash,
                    role = UserRole.ADMIN.name,
                    active = true,
                ),
            )

            UserRole.SIGNER -> signerDao.save(
                SignerEntity(
                    id = id,
                    fullName = normalizedName,
                    email = normalizedEmail,
                    active = true,
                    phone = phone.trim(),
                    passwordHash = passwordHash,
                ),
            )
        }
        return RegistrationResult(
            user = AuthenticatedUser(id, normalizedEmail, normalizedName, role),
        )
    }
}
