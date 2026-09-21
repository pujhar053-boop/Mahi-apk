package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScratchNote
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM scratch_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<ScratchNote>>

    @Query("SELECT * FROM scratch_notes WHERE id = :id")
    suspend fun getNoteById(id: Long): ScratchNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: ScratchNote): Long

    @Update
    suspend fun updateNote(note: ScratchNote)

    @Delete
    suspend fun deleteNote(note: ScratchNote)

    @Query("DELETE FROM scratch_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
}
