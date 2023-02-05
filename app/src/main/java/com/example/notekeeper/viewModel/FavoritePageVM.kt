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
class FavoritePageVM @Inject constructor(private val repository: NoteRepository) : ViewModel()
{
    val empty = MutableLiveData<Boolean>()
    val favoriteNotesList = MutableLiveData<MutableList<NoteEntity>>()

    fun loadFavorite() = viewModelScope.launch {

        val response = repository.getAllFavorites()
        if(response.isNotEmpty())
        {
            favoriteNotesList.postValue(response)
            empty.postValue(false)
        }
        else
            empty.postValue(true)

    }
}