package br.com.fiap.fides.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.fiap.fides.data.local.entity.DocumentEntity
import br.com.fiap.fides.data.local.entity.DocumentSignerEntity
import br.com.fiap.fides.data.local.entity.SignatureFieldEntity

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE ownerId = :userId ORDER BY updatedAtEpochMillis DESC")
    suspend fun getAllForUser(userId: String): List<DocumentEntity>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(document: DocumentEntity)

    @Query("SELECT * FROM document_signers WHERE documentId = :documentId")
    suspend fun getSigners(documentId: String): List<DocumentSignerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSigners(signers: List<DocumentSignerEntity>)

    @Query("DELETE FROM document_signers WHERE documentId = :documentId")
    suspend fun deleteSigners(documentId: String)

    @Query("SELECT * FROM signature_fields WHERE documentId = :documentId")
    suspend fun getSignatureFields(documentId: String): List<SignatureFieldEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSignatureFields(fields: List<SignatureFieldEntity>)

    @Query("DELETE FROM signature_fields WHERE documentId = :documentId")
    suspend fun deleteSignatureFields(documentId: String)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteById(id: String)

    @Delete
    suspend fun delete(document: DocumentEntity)
}
