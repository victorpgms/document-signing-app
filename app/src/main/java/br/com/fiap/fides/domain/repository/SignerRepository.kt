package br.com.fiap.fides.domain.repository

import br.com.fiap.fides.domain.model.Signer

interface SignerRepository {
    suspend fun getAll(): List<Signer>

    suspend fun getById(id: String): Signer?

    suspend fun save(signer: Signer)

    suspend fun delete(id: String)
}
