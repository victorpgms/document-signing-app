package br.com.fiap.fides.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.fiap.fides.data.local.dao.DocumentDao
import br.com.fiap.fides.data.local.dao.SignerDao
import br.com.fiap.fides.data.local.entity.DocumentEntity
import br.com.fiap.fides.data.local.entity.DocumentSignerEntity
import br.com.fiap.fides.data.local.entity.SignatureFieldEntity
import br.com.fiap.fides.data.local.entity.SignerEntity

@Database(
    entities = [
        SignerEntity::class,
        DocumentEntity::class,
        DocumentSignerEntity::class,
        SignatureFieldEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class FidesDatabase : RoomDatabase() {
    abstract fun signerDao(): SignerDao

    abstract fun documentDao(): DocumentDao
}
