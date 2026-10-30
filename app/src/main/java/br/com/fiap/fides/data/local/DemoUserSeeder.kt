package br.com.fiap.fides.data.local

import br.com.fiap.fides.core.security.PasswordHasher
import br.com.fiap.fides.data.local.entity.SignerEntity
import br.com.fiap.fides.data.local.entity.UserEntity
import java.util.UUID

object DemoUserSeeder {
    const val EMAIL = "victor@fides.local"
    const val PASSWORD = "fides1234"
    const val SIGNER_EMAIL = "ana.teste@fides.local"
    const val SIGNER_PASSWORD = "fides1234"

    suspend fun ensure(database: FidesDatabase) {
        val dao = database.userDao()
        if (dao.getByEmail(EMAIL) == null) {
            dao.insert(
                UserEntity(
                    id = UUID.randomUUID().toString(),
                    email = EMAIL,
                    displayName = "Victor",
                    passwordHash = PasswordHasher().hash(PASSWORD.toCharArray()),
                    role = "ADMIN",
                    active = true,
                ),
            )
        }
        val signerDao = database.signerDao()
        val signer = signerDao.getByEmail(SIGNER_EMAIL)
        when {
            signer == null -> signerDao.save(
                SignerEntity(
                    id = UUID.randomUUID().toString(),
                    fullName = "Ana Teste",
                    email = SIGNER_EMAIL,
                    active = true,
                    phone = "(11) 90000-0000",
                    passwordHash = PasswordHasher().hash(SIGNER_PASSWORD.toCharArray()),
                ),
            )

            else -> signerDao.save(
                signer.copy(
                    passwordHash = PasswordHasher().hash(SIGNER_PASSWORD.toCharArray()),
                ),
            )
        }
    }
}
