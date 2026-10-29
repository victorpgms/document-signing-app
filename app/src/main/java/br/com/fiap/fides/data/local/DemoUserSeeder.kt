package br.com.fiap.fides.data.local

import br.com.fiap.fides.core.security.PasswordHasher
import br.com.fiap.fides.data.local.entity.UserEntity
import java.util.UUID

object DemoUserSeeder {
    const val EMAIL = "victor@fides.local"
    const val PASSWORD = "fides1234"

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
    }
}
