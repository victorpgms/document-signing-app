package br.com.fiap.fides.data.repository

import br.com.fiap.fides.domain.model.Signer
import br.com.fiap.fides.domain.model.UserRole
import br.com.fiap.fides.domain.repository.AuthenticatedUser
import br.com.fiap.fides.domain.repository.SessionRepository
import br.com.fiap.fides.domain.security.AccessDeniedException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test

class AdminOnlySignerRepositoryTest {
    @Test
    fun `rejects signer session before accessing delegate`() = runBlocking {
        val delegate = RecordingSignerRepository()
        val repository = AdminOnlySignerRepository(delegate, FakeSessionRepository(UserRole.SIGNER))

        assertThrows(AccessDeniedException::class.java) { runBlocking { repository.getAll() } }
        org.junit.Assert.assertEquals(0, delegate.reads)
    }
}

private class FakeSessionRepository(role: UserRole) : SessionRepository {
    override val session: Flow<br.com.fiap.fides.domain.repository.UserSession?> = flowOf(
        br.com.fiap.fides.domain.repository.UserSession("user", "user@fides.local", "User", role),
    )

    override suspend fun save(user: AuthenticatedUser) = Unit

    override suspend fun clear() = Unit
}

private class RecordingSignerRepository : br.com.fiap.fides.domain.repository.SignerRepository {
    var reads = 0

    override suspend fun getAll(): List<Signer> {
        reads += 1
        return emptyList()
    }

    override suspend fun getById(id: String): Signer? = null

    override suspend fun save(signer: Signer) = Unit

    override suspend fun delete(id: String) = Unit
}
