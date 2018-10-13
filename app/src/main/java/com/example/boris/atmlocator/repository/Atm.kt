package com.example.boris.atmlocator.repository

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * Data class for an ATM used to store the atmsLiveData in a Room database
 */
@Entity
data class Atm(
        @PrimaryKey(autoGenerate = true) var id: Long?,
        val name: String,
        val latitude: Double,
        val longitude: Double,
        @Embedded val address: Address,
        var distance: Float? = null)

data class Address(val formatted: String)
