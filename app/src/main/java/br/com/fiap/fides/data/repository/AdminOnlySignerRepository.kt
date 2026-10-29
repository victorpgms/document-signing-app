package br.com.fiap.fides.data.repository

import br.com.fiap.fides.domain.model.Signer
import br.com.fiap.fides.domain.model.UserRole
import br.com.fiap.fides.domain.repository.SessionRepository
import br.com.fiap.fides.domain.repository.SignerRepository
import br.com.fiap.fides.domain.security.AccessDeniedException
import kotlinx.coroutines.flow.first

class AdminOnlySignerRepository(
    private val delegate: SignerRepository,
    private val sessionRepository: SessionRepository,
) : SignerRepository {
    override suspend fun getAll(): List<Signer> {
        requireAdmin()
        return delegate.getAll()
    }

    override suspend fun getById(id: String): Signer? {
        requireAdmin()
        return delegate.getById(id)
    }

    override suspend fun save(signer: Signer) {
        requireAdmin()
        delegate.save(signer)
    }

    override suspend fun delete(id: String) {
        requireAdmin()
        delegate.delete(id)
    }

    private suspend fun requireAdmin() {
        if (sessionRepository.session.first()?.role != UserRole.ADMIN) {
            throw AccessDeniedException()
        }
    }
}
