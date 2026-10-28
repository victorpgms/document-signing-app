package br.com.fiap.fides.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.fiap.fides.data.local.entity.SignerEntity

@Dao
interface SignerDao {
    @Query("SELECT * FROM signers ORDER BY fullName")
    suspend fun getAll(): List<SignerEntity>

    @Query("SELECT * FROM signers WHERE id = :id")
    suspend fun getById(id: String): SignerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(signer: SignerEntity)

    @Delete
    suspend fun delete(signer: SignerEntity)
}
