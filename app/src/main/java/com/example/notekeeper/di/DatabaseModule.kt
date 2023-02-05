package com.example.notekeeper.di

import android.content.Context
import androidx.room.Room
import com.example.notekeeper.database.NoteDatabase
import com.example.notekeeper.database.NoteEntity
import com.example.notekeeper.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule
{
    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context, NoteDatabase::class.java, Constants.DATA_BASE_NAME)
        .allowMainThreadQueries()
        .fallbackToDestructiveMigration()
        .build()

    @Singleton
    @Provides
    fun provideNoteEntity(): NoteEntity = NoteEntity()

    @Singleton
    @Provides
    fun provideNoteDao(db: NoteDatabase) = db.noteDao()
}