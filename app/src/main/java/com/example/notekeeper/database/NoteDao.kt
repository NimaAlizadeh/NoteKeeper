package com.example.notekeeper.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.notekeeper.utils.Constants

@Dao
interface NoteDao
{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(entity: NoteEntity)

    @Delete
    suspend fun deleteNote(entity: NoteEntity)

    @Update
    suspend fun updateNote(entity: NoteEntity)

    @Query(""+"SELECT * FROM ${Constants.DATA_BASE_TABLE_NOTE_NAME} " +
            "ORDER BY CASE WHEN :order = 'title_asc' THEN title END ASC ,CASE WHEN :order = 'title_desc' THEN title END DESC ," +
            "CASE WHEN :order = 'date_asc' THEN date END ASC ,CASE WHEN :order = 'date_desc' THEN date END DESC ," +
            "CASE WHEN :order = 'priority_low' THEN priority END ASC ,CASE WHEN :order = 'priority_high' THEN priority END DESC ," +
            "CASE WHEN :order = 'id' THEN id END")
    fun getAllNotes(order: String): LiveData<MutableList<NoteEntity>>

    @Query(""+"SELECT * FROM ${Constants.DATA_BASE_TABLE_NOTE_NAME} WHERE id =:noteId")
    suspend fun getSingleNote(noteId: Int): NoteEntity

    @Query(""+"SELECT * FROM ${Constants.DATA_BASE_TABLE_NOTE_NAME} WHERE title LIKE '%' || :title || '%'")
    suspend fun getSearchedNotes(title: String): MutableList<NoteEntity>

    @Query(""+"SELECT isFavorite FROM ${Constants.DATA_BASE_TABLE_NOTE_NAME} WHERE id =:noteId")
    suspend fun isFavorite(noteId: Int): Boolean

    @Query(""+"UPDATE ${Constants.DATA_BASE_TABLE_NOTE_NAME} SET isFavorite =:favorite WHERE id = :noteId")
    suspend fun setFavorite(noteId: Int, favorite: Boolean)

    @Query(""+"SELECT * FROM ${Constants.DATA_BASE_TABLE_NOTE_NAME} WHERE isFavorite = 1")
    suspend fun getAllFavoriteNotes(): MutableList<NoteEntity>
}