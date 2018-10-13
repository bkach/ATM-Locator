package com.example.boris.atmlocator.repository

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query

/**
 * Room Data Access Object used to communicate with the Room database
 */
@Dao
interface AtmDao {
    @Insert(onConflict = REPLACE)
    fun save(atms: List<Atm>)

    @Query("SELECT * FROM atm")
    fun load(): LiveData<List<Atm>>

    @Query("DELETE FROM atm")
    fun deleteAll()
}