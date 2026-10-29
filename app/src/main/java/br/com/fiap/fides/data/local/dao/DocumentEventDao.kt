package br.com.fiap.fides.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.fiap.fides.data.local.entity.DocumentEventEntity

@Dao
interface DocumentEventDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(event: DocumentEventEntity)

    @Query("SELECT * FROM document_events WHERE documentId = :documentId ORDER BY occurredAtEpochMillis ASC")
    suspend fun getForDocument(documentId: String): List<DocumentEventEntity>
}
