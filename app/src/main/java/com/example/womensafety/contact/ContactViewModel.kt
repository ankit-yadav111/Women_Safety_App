package com.example.womensafety.contact

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactViewModal(application: Application) : AndroidViewModel(application) {
    val allcont: LiveData<List<contact>>
    private val repository : ContactReporitory

    init {
        val dao = ContactDatabase.getDatabase(application).contactDao()
        repository= ContactReporitory(dao)
        allcont=repository.allCont
    }

    fun deleteNote(note:contact) = viewModelScope.launch(Dispatchers.IO){
        repository.delete(note)
    }

    fun insertNote(note: contact) = viewModelScope.launch(Dispatchers.IO){
        Log.e("Ankit","Insert")
        repository.insert(note)
    }
}