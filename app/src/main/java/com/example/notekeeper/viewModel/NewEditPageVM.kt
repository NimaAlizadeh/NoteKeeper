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
class NewEditPageVM @Inject constructor(private val repository: NoteRepository) : ViewModel()
{
    var singleNote = MutableLiveData<NoteEntity>()
    var loading = MutableLiveData<Boolean>()

    fun saveNote(entity: NoteEntity) = viewModelScope.launch {
        repository.saveNote(entity)
    }

    fun editNote(entity: NoteEntity) = viewModelScope.launch {
        repository.updateNote(entity)
    }

    fun loadSingleNote(id: Int) = viewModelScope.launch {
        loading.postValue(true)

        val response = repository.getSingleNote(id)
        singleNote.postValue(response)

        loading.postValue(false)
    }
}