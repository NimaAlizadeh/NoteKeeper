package com.example.notekeeper.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notekeeper.database.NoteEntity
import com.example.notekeeper.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailPageVM @Inject constructor(private val repository: NoteRepository) : ViewModel()
{
    val loading = MutableLiveData<Boolean>()
    val details = MutableLiveData<NoteEntity>()
    val textAlignment = MutableLiveData<String>()
    val isFavorite = MutableLiveData<Boolean>()

    fun loadDetail(id: Int) = viewModelScope.launch {
        loading.postValue(true)

        val response = repository.getSingleNote(id)
        if(response.id != 0)
            details.postValue(response)

        loading.postValue(false)
    }

    fun deleteNote(entity: NoteEntity) = viewModelScope.launch {
        repository.deleteNote(entity)
    }

    fun setTextAlignment(order: String){
        textAlignment.postValue(order)
    }

    fun setFavorite(id: Int) = viewModelScope.launch {
        if(repository.isFavorite(id))
        {
            isFavorite.postValue(false)
            repository.setFavorite(id, false)
        }
        else
        {
            isFavorite.postValue(true)
            repository.setFavorite(id, true)
        }
    }

    fun isFavorite(id: Int) = viewModelScope.launch{
        if(repository.isFavorite(id))
            isFavorite.postValue(true)
        else
            isFavorite.postValue(false)
    }
}