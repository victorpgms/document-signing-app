package br.com.fiap.fides.data.repository

import br.com.fiap.fides.data.local.dao.SignerDao
import br.com.fiap.fides.data.local.entity.SignerEntity
import br.com.fiap.fides.domain.model.Signer
import br.com.fiap.fides.domain.repository.SignerRepository

class LocalSignerRepository(private val dao: SignerDao) : SignerRepository {
    override suspend fun getAll(): List<Signer> = dao.getAll().map { it.toDomain() }

    override suspend fun getById(id: String): Signer? = dao.getById(id)?.toDomain()

    override suspend fun save(signer: Signer) {
        dao.save(signer.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.getById(id)?.let { signer -> dao.delete(signer) }
    }
}

private fun SignerEntity.toDomain() = Signer(id, fullName, email, active, phone, passwordHash)

private fun Signer.toEntity() = SignerEntity(id, fullName, email, active, phone, passwordHash)
