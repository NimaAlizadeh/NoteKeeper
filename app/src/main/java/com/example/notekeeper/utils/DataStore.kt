package com.example.notekeeper.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class DataStore @Inject constructor(@ApplicationContext val context: Context)
{
    companion object{
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.DATASTORE_NAME_USER)

        val textAlignment = stringPreferencesKey(Constants.DATASTORE_ALIGNMENT)
        val listShape = stringPreferencesKey(Constants.LIST_SHAPE)
    }

    suspend fun setTextAlignment(alignment: String)
    {
        //here we just save our string value in the datastore as a string (actually we are editing the value that have been saved before)
        context.dataStore.edit {
            it[textAlignment] = alignment
        }
    }

    fun getTextAlignment() = context.dataStore.data.map {
        //give me the textAlignment from datastore and if it was empty just give me an empty string
        it[textAlignment] ?: ""
    }

    suspend fun setListShape(shape: String)
    {
        context.dataStore.edit {
            it[listShape] = shape
        }
    }

    fun getListShape() = context.dataStore.data.map {
        it[listShape] ?: ""
    }
}
