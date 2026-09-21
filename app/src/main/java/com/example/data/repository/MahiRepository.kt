package com.example.data.repository

import com.example.data.db.ActionDao
import com.example.data.db.NoteDao
import com.example.data.model.ActionStatus
import com.example.data.model.LiveActionItem
import com.example.data.model.ScratchNote
import kotlinx.coroutines.flow.Flow

class MahiRepository(
    private val noteDao: NoteDao,
    private val actionDao: ActionDao
) {
    val allNotes: Flow<List<ScratchNote>> = noteDao.getAllNotes()
    val recentActions: Flow<List<LiveActionItem>> = actionDao.getRecentActions()

    suspend fun saveNote(title: String, content: String, existingId: Long? = null): Long {
        val now = System.currentTimeMillis()
        val note = if (existingId != null && existingId > 0) {
            ScratchNote(id = existingId, title = title, content = content, updatedAt = now)
        } else {
            ScratchNote(title = title, content = content, createdAt = now, updatedAt = now)
        }
        val id = noteDao.insertNote(note)
        recordAction(
            actionName = if (existingId != null && existingId > 0) "Updated Note" else "Created Note",
            status = ActionStatus.SUCCESS,
            detail = title.ifBlank { "Untitled Note" }
        )
        return id
    }

    suspend fun deleteNote(id: Long) {
        val note = noteDao.getNoteById(id)
        noteDao.deleteNoteById(id)
        recordAction(
            actionName = "Deleted Note",
            status = ActionStatus.SUCCESS,
            detail = note?.title ?: "Note #$id"
        )
    }

    suspend fun recordAction(actionName: String, status: ActionStatus, detail: String): Long {
        return actionDao.insertAction(
            LiveActionItem(
                actionName = actionName,
                status = status,
                detail = detail,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearActions() {
        actionDao.clearActions()
    }
}
