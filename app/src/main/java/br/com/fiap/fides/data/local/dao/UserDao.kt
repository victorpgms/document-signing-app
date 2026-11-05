package br.com.fiap.fides.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.fiap.fides.data.local.entity.UserEntity
import br.com.fiap.fides.domain.model.UserRole

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'ADMIN' AND active = 1 ORDER BY displayName")
    suspend fun getActiveAdmins(): List<UserEntity>

    @Query("UPDATE users SET displayName = :displayName, email = :email, phone = :phone WHERE id = :id")
    suspend fun updateContact(id: String, displayName: String, email: String, phone: String)

    @Query("UPDATE users SET passwordHash = :passwordHash WHERE id = :id")
    suspend fun updatePassword(id: String, passwordHash: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity)
}
