package com.example.boris.atmlocator.repository

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

/**
 * Room database used to fetch the AtmDao.
 *
 * This class' annotations are also used to communicate with the database, and can be useful
 * when migrating atmsLiveData
 */
@Database(entities = [Atm::class], version = 1, exportSchema = false)
abstract class AtmDatabase : RoomDatabase() {
    abstract fun atmDao(): AtmDao
}