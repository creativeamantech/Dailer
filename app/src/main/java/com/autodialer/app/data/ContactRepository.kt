package com.autodialer.app.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactRepository(private val contactDao: ContactDao) {

    val allContacts: LiveData<List<Contact>> = contactDao.getAllContacts()

    suspend fun insertContacts(contacts: List<Contact>) {
        withContext(Dispatchers.IO) {
            contactDao.clearAllContacts() // Start fresh for new session
            contactDao.insertContacts(contacts)
        }
    }

    suspend fun getAllContactsSync(): List<Contact> {
        return withContext(Dispatchers.IO) {
            contactDao.getAllContactsSync()
        }
    }

    suspend fun updateContact(contact: Contact) {
        withContext(Dispatchers.IO) {
            contactDao.updateContact(contact)
        }
    }
}
