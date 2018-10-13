package com.example.boris.atmlocator.repository

import android.arch.persistence.room.Room
import android.content.Context
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

/**
 * Wrapper class used to abstract the context away from the Repository in order to make the Repository
 * testable
 */
class AtmDatabaseFactory : KoinComponent {

    private val context: Context by inject()

    fun getDatabase() : AtmDatabase {
        return Room.databaseBuilder(context, AtmDatabase::class.java, "atm-db").build()
    }
}