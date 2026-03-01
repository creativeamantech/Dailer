package com.autodialer.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.autodialer.app.data.AppDatabase
import com.autodialer.app.data.Contact
import com.autodialer.app.data.ContactRepository
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ContactRepository
    val allContacts: LiveData<List<Contact>>

    init {
        val contactDao = AppDatabase.getDatabase(application).contactDao()
        repository = ContactRepository(contactDao)
        allContacts = repository.allContacts
    }

    suspend fun insertContacts(contacts: List<Contact>) {
        repository.insertContacts(contacts)
    }

    fun updateContact(contact: Contact) = viewModelScope.launch {
        repository.updateContact(contact)
    }

    suspend fun getAllContactsSync(): List<Contact> {
        return repository.getAllContactsSync()
    }
}
