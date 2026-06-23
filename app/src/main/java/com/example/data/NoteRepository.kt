package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NoteRepository(private val noteDao: NoteDao) {
    val allNotesFlow: Flow<List<DecryptedNote>> = noteDao.getAllNotesFlow().map { notesList ->
        notesList.map { it.toDecryptedNote() }
    }

    suspend fun getNoteById(id: Int): DecryptedNote? {
        return noteDao.getNoteById(id)?.toDecryptedNote()
    }

    suspend fun saveNote(decryptedNote: DecryptedNote): Long {
        val encryptedNote = decryptedNote.toEncryptedNote()
        return noteDao.insert(encryptedNote)
    }

    suspend fun deleteNote(decryptedNote: DecryptedNote) {
        val encryptedNote = decryptedNote.toEncryptedNote()
        noteDao.delete(encryptedNote)
    }

    suspend fun clearAllNotes() {
        noteDao.deleteAll()
    }
}
