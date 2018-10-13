package com.example.boris.atmlocator.repository

import android.arch.persistence.room.Room
import android.content.Context
import org.koin.java.standalone.KoinJavaComponent.inject

/**
 * Wrapper class used to abstract the context away from the Repository in order to make the Repository
 * testable
 */
class AtmDatabaseFactory {

    private val context: Context by inject(Context::class.java)

    fun getDatabase() : AtmDatabase {
        return Room.databaseBuilder(context, AtmDatabase::class.java, "atm-db").build()
    }
}