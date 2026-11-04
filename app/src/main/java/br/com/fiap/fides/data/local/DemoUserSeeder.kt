package br.com.fiap.fides.data.local

import br.com.fiap.fides.core.security.PasswordHasher
import br.com.fiap.fides.data.local.entity.SignerEntity
import br.com.fiap.fides.data.local.entity.UserEntity
import br.com.fiap.fides.data.local.dao.SignerDao
import java.util.UUID

object DemoUserSeeder {
    const val EMAIL = "admin.victor@fides.local"
    const val PASSWORD = "fides1234"
    const val SECOND_ADMIN_EMAIL = "admin.lucas@fides.local"
    const val SECOND_ADMIN_PASSWORD = "fides1234"
    const val SIGNER_EMAIL = "ana.teste@fides.local"
    const val SIGNER_PASSWORD = "fides1234"
    const val SECOND_SIGNER_EMAIL = "bruno.teste@fides.local"
    const val SECOND_SIGNER_PASSWORD = "fides1234"

    suspend fun ensure(database: FidesDatabase) {
        val dao = database.userDao()
        ensureAdmin(dao, EMAIL, "Victor Almeida", PASSWORD)
        ensureAdmin(dao, SECOND_ADMIN_EMAIL, "Lucas", SECOND_ADMIN_PASSWORD)
        val signerDao = database.signerDao()
        ensureSigner(signerDao, "Ana Teste", SIGNER_EMAIL, SIGNER_PASSWORD, "(11) 90000-0000")
        ensureSigner(signerDao, "Bruno Teste", SECOND_SIGNER_EMAIL, SECOND_SIGNER_PASSWORD, "(11) 91111-1111")
    }

    private suspend fun ensureAdmin(
        dao: br.com.fiap.fides.data.local.dao.UserDao,
        email: String,
        displayName: String,
        password: String,
    ) {
        val existing = dao.getByEmail(email)
        if (existing == null) {
            dao.insert(
                UserEntity(
                    id = UUID.randomUUID().toString(),
                    email = email,
                    displayName = displayName,
                    passwordHash = PasswordHasher().hash(password.toCharArray()),
                    role = "ADMIN",
                    active = true,
                ),
            )
        }
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
