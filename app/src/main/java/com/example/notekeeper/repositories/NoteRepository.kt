package com.example.notekeeper.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.notekeeper.database.NoteDao
import com.example.notekeeper.database.NoteEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NoteRepository @Inject constructor(private val dao: NoteDao, @ApplicationContext private val context: Context)
{
    suspend fun saveNote(entity: NoteEntity) = dao.insertNote(entity)
    suspend fun deleteNote(entity: NoteEntity) = dao.deleteNote(entity)
    suspend fun updateNote(entity: NoteEntity) = dao.updateNote(entity)

    fun getNotes(order: String): LiveData<MutableList<NoteEntity>> = dao.getAllNotes(order)
    suspend fun getSingleNote(id: Int): NoteEntity = dao.getSingleNote(id)
    suspend fun getSearchedNotes(title: String) = dao.getSearchedNotes(title)

    suspend fun setFavorite(id: Int, isFavorite: Boolean) = dao.setFavorite(id, isFavorite)
    suspend fun isFavorite(id: Int) = dao.isFavorite(id)
    suspend fun getAllFavorites() = dao.getAllFavoriteNotes()
}