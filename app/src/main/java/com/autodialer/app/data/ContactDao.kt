package com.autodialer.app.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<Contact>)

    @Query("SELECT * FROM contacts ORDER BY id ASC")
    fun getAllContacts(): LiveData<List<Contact>>

    @Query("SELECT * FROM contacts ORDER BY id ASC")
    suspend fun getAllContactsSync(): List<Contact>

    @Update
    suspend fun updateContact(contact: Contact)

    @Query("DELETE FROM contacts")
    suspend fun clearAllContacts()
}
