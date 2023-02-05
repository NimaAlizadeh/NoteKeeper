package com.example.notekeeper.database

import android.webkit.WebSettings
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.notekeeper.utils.Constants
import java.util.*

@Entity(tableName = Constants.DATA_BASE_TABLE_NOTE_NAME)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var title: String = "",
    var desc: String = "",
    var date: Long = 0L,
    var priority: Int = 0,
    var isFavorite: Boolean = false
)