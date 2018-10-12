package com.example.boris.atmlocator

import android.os.Bundle
import com.google.android.gms.location.FusedLocationProviderClient

class LocationManager {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private val KEY_LOCATION = "location"
    }

    fun saveInstanceState(outState: Bundle) : Bundle {
//        outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        return outState
    }

}