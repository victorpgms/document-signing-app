package br.com.fiap.fides

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.fiap.fides.data.local.DemoUserSeeder
import br.com.fiap.fides.data.local.FidesDatabase
import br.com.fiap.fides.data.repository.LocalAuthRepository
import br.com.fiap.fides.data.repository.LocalDocumentRepository
import br.com.fiap.fides.domain.model.Document
import br.com.fiap.fides.domain.model.DocumentSigner
import br.com.fiap.fides.domain.model.DocumentStatus
import br.com.fiap.fides.domain.model.SignerRole
import br.com.fiap.fides.domain.model.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class LocalSigningWorkflowInstrumentedTest {
    private lateinit var database: FidesDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FidesDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun signerAuthenticatesAndSeesOnlyLinkedDocument() = runBlocking {
        DemoUserSeeder.ensure(database)
        val authRepository = LocalAuthRepository(database.userDao(), database.signerDao())
        val authenticated = authRepository.authenticate(
            DemoUserSeeder.SIGNER_EMAIL,
            DemoUserSeeder.SIGNER_PASSWORD.toCharArray(),
        )

        assertNotNull(authenticated)
        assertEquals(UserRole.SIGNER, authenticated?.role)

        val repository = LocalDocumentRepository(database)
        val now = Instant.EPOCH
        repository.save(
            Document(
                id = "linked-document",
                ownerId = "admin",
                title = "Documento vinculado",
                originalFileName = "documento.pdf",
                localFilePath = "/private/documento.pdf",
                status = DocumentStatus.SENT,
                createdAt = now,
                updatedAt = now,
                signers = listOf(
                    DocumentSigner(
                        documentId = "linked-document",
                        signerId = authenticated!!.id,
                        role = SignerRole.CONTRACTOR,
                    ),
                ),
            ),
        )

        val visibleDocuments = repository.getAllForUser(authenticated.id)
        assertEquals(listOf("linked-document"), visibleDocuments.map { it.id })
    }
}
