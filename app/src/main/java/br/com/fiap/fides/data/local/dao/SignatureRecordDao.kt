package br.com.fiap.fides.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.fiap.fides.data.local.entity.SignatureRecordEntity

@Dao
interface SignatureRecordDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: SignatureRecordEntity)

    @Query("SELECT * FROM signature_records WHERE documentId = :documentId ORDER BY createdAtEpochMillis ASC")
    suspend fun getForDocument(documentId: String): List<SignatureRecordEntity>
}
