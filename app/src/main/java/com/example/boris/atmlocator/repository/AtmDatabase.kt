package com.example.boris.atmlocator.repository

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = [Atm::class], version = 1, exportSchema = false)
abstract class AtmDatabase : RoomDatabase() {
    abstract fun atmDao(): AtmDao
}