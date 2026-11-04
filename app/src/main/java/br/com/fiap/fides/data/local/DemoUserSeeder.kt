package br.com.fiap.fides.data.local

import br.com.fiap.fides.core.security.PasswordHasher
import br.com.fiap.fides.data.local.entity.SignerEntity
import br.com.fiap.fides.data.local.entity.UserEntity
import br.com.fiap.fides.data.local.dao.SignerDao
import java.util.UUID

object DemoUserSeeder {
    const val EMAIL = "victor@fides.local"
    const val PASSWORD = "fides1234"
    const val SIGNER_EMAIL = "ana.teste@fides.local"
    const val SIGNER_PASSWORD = "fides1234"
    const val SECOND_SIGNER_EMAIL = "bruno.teste@fides.local"
    const val SECOND_SIGNER_PASSWORD = "fides1234"

    suspend fun ensure(database: FidesDatabase) {
        val dao = database.userDao()
        val existingAdmin = dao.getByEmail(EMAIL)
        val adminUser = existingAdmin ?: UserEntity(
                    id = UUID.randomUUID().toString(),
                    email = EMAIL,
                    displayName = "Victor Almeida",
                    passwordHash = PasswordHasher().hash(PASSWORD.toCharArray()),
                    role = "ADMIN",
                    active = true,
                ).also { dao.insert(it) }
        val signerDao = database.signerDao()
        ensureSigner(
            signerDao = signerDao,
            fullName = "Victor Almeida",
            email = EMAIL,
            password = PASSWORD,
            phone = "(11) 98888-8888",
            preferredId = adminUser.id,
        )
        ensureSigner(signerDao, "Ana Teste", SIGNER_EMAIL, SIGNER_PASSWORD, "(11) 90000-0000")
        ensureSigner(signerDao, "Bruno Teste", SECOND_SIGNER_EMAIL, SECOND_SIGNER_PASSWORD, "(11) 91111-1111")
    }

    private suspend fun ensureSigner(
        signerDao: SignerDao,
        fullName: String,
        email: String,
        password: String,
        phone: String,
        preferredId: String? = null,
    ) {
        val signer = signerDao.getByEmail(email)
        when {
            signer == null -> signerDao.save(
                SignerEntity(
                    id = preferredId ?: UUID.randomUUID().toString(),
                    fullName = fullName,
                    email = email,
                    active = true,
                    phone = phone,
                    passwordHash = PasswordHasher().hash(password.toCharArray()),
                ),
            )

            else -> signerDao.save(
                signer.copy(
                    passwordHash = PasswordHasher().hash(password.toCharArray()),
                ),
            )
        }
    }
}
