package br.com.fiap.fides.data.repository

import br.com.fiap.fides.data.local.dao.SignerDao
import br.com.fiap.fides.data.local.dao.UserDao
import br.com.fiap.fides.data.local.entity.SignerEntity
import br.com.fiap.fides.domain.model.Signer
import br.com.fiap.fides.domain.model.UserRole
import br.com.fiap.fides.domain.repository.SignerRepository

class LocalSignerRepository(
    private val dao: SignerDao,
    private val userDao: UserDao? = null,
) : SignerRepository {
    override suspend fun getAll(): List<Signer> {
        val signers = dao.getAll().map { it.toDomain() }
        val admins = userDao?.getActiveAdmins().orEmpty().map { user ->
            Signer(
                id = user.id,
                fullName = user.displayName,
                email = user.email,
                active = user.active,
                phone = user.phone,
                accountRole = UserRole.ADMIN,
            )
        }
        return (signers + admins).distinctBy { it.id }.sortedBy { it.fullName.lowercase() }
    }

    override suspend fun getById(id: String): Signer? = dao.getById(id)?.toDomain()
        ?: userDao?.getActiveAdmins()?.firstOrNull { it.id == id }?.let { user ->
            Signer(user.id, user.displayName, user.email, user.active, user.phone, accountRole = UserRole.ADMIN)
        }

    override suspend fun save(signer: Signer) {
        dao.save(signer.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.getById(id)?.let { signer -> dao.delete(signer) }
    }
}

private fun SignerEntity.toDomain() = Signer(id, fullName, email, active, phone, passwordHash, UserRole.SIGNER)

private fun Signer.toEntity() = SignerEntity(id, fullName, email, active, phone, passwordHash)
