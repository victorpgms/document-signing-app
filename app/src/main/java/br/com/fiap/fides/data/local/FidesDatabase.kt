package br.com.fiap.fides.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.fiap.fides.data.local.dao.DocumentDao
import br.com.fiap.fides.data.local.dao.SignerDao
import br.com.fiap.fides.data.local.dao.SignatureRecordDao
import br.com.fiap.fides.data.local.dao.UserDao
import br.com.fiap.fides.data.local.entity.DocumentEntity
import br.com.fiap.fides.data.local.entity.DocumentEventEntity
import br.com.fiap.fides.data.local.entity.DocumentSignerEntity
import br.com.fiap.fides.data.local.entity.SignatureFieldEntity
import br.com.fiap.fides.data.local.entity.SignerEntity
import br.com.fiap.fides.data.local.entity.UserEntity
import br.com.fiap.fides.data.local.entity.SignatureRecordEntity

@Database(
    entities = [
        SignerEntity::class,
        DocumentEntity::class,
        DocumentEventEntity::class,
        DocumentSignerEntity::class,
        SignatureFieldEntity::class,
        UserEntity::class,
        SignatureRecordEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
abstract class FidesDatabase : RoomDatabase() {
    abstract fun signerDao(): SignerDao

    abstract fun documentDao(): DocumentDao

    abstract fun documentEventDao(): br.com.fiap.fides.data.local.dao.DocumentEventDao

    abstract fun userDao(): UserDao

    abstract fun signatureRecordDao(): SignatureRecordDao
}
