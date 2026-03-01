package com.autodialer.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    // Store JSON or delimited string for extra dynamic columns
    val extraData: String,
    // Track if it has been called, and the outcome
    var hasBeenCalled: Boolean = false,
    var callOutcome: String? = null,
    var notes: String? = null
)
