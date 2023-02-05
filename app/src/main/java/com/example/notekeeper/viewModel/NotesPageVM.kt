package com.example.notekeeper.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notekeeper.database.NoteEntity
import com.example.notekeeper.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesPageVM @Inject constructor(private val repository: NoteRepository) : ViewModel()
{
    private var noteList = mutableListOf<NoteEntity>()
    var noteListLiveData = MutableLiveData<MutableList<NoteEntity>>()
    val emptyList = MutableLiveData<Boolean>()
    val orders = MutableLiveData<String>()

    fun loadNoteList() = viewModelScope.launch {
        if(noteList.isNotEmpty())
        {
            noteListLiveData.postValue(noteList)
            emptyList.postValue(false)
        }
        else
        {
            emptyList.postValue(true)
        }
    }

    fun setObservedList(observedList: MutableList<NoteEntity>)
    {
        noteList = observedList
    }

    fun deleteNote(entity: NoteEntity) = viewModelScope.launch {
        repository.deleteNote(entity)
    }

    fun getSearchedNotes(title: String) = viewModelScope.launch {
        val response = repository.getSearchedNotes(title)
        setObservedList(response)
        loadNoteList()
    }
}