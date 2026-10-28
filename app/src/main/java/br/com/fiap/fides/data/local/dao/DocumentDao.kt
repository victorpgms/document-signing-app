package br.com.fiap.fides.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.fiap.fides.data.local.entity.DocumentEntity

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE ownerId = :userId ORDER BY updatedAtEpochMillis DESC")
    suspend fun getAllForUser(userId: String): List<DocumentEntity>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(document: DocumentEntity)

    @Delete
    suspend fun delete(document: DocumentEntity)
}
