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

    @Query("SELECT * FROM signers WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): SignerEntity?

    @Query("UPDATE signers SET fullName = :fullName, email = :email, phone = :phone WHERE id = :id")
    suspend fun updateContact(id: String, fullName: String, email: String, phone: String)

    @Query("UPDATE signers SET passwordHash = :passwordHash WHERE id = :id")
    suspend fun updatePassword(id: String, passwordHash: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(signer: SignerEntity)

    @Delete
    suspend fun delete(signer: SignerEntity)
}
