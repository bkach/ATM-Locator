/*
 * ATMLocator
 * Copyright (C) 2018 Boris Kachscovsky
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.example.boris.atmlocator

import android.annotation.SuppressLint
import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.example.boris.atmlocator.repository.Atm
import com.example.boris.atmlocator.util.DistanceCalculator
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * Class used to control and inform the rest of the app about Location.
 *
 * Includes functionality for permissions, current location, and distance
 */
class LocationManager {

    private lateinit var activity: Activity
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Emits the last known location, even if location is currently unavailable
    val lastKnownLocation : MutableLiveData<Location?> = MutableLiveData()
    private lateinit var lastKnownLocationObserver: Observer<Location?>

    private val distanceCalculator = DistanceCalculator()
    var locationPermissionGranted: Boolean = false
            private set

    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 123
        private const val KEY_LOCATION = "location"
    }

    fun init(activity: Activity) {
        this.activity = activity
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
    }

    fun saveInstanceState(outState: Bundle) {
        outState.putParcelable(KEY_LOCATION, lastKnownLocation.value)
    }

    fun restoreInstanceState(savedInstanceState: Bundle) {
        lastKnownLocation.value = savedInstanceState.getParcelable<Location?>(KEY_LOCATION)
    }

    fun getLocationPermission(onLocationPermissionGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(activity.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            onLocationPermissionGranted()
        } else {
            ActivityCompat.requestPermissions(activity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    fun onLocationPermissionResult(locationPermissionGranted: Boolean) {
        this.locationPermissionGranted = locationPermissionGranted
        if (!locationPermissionGranted) {
            lastKnownLocation.value = null
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshLastKnownLocation(onLocationRetrieved: (Boolean, Exception?) -> Unit) {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(activity) {task ->
                    if (task.isSuccessful) {
                        lastKnownLocation.value = task.result
                        if (lastKnownLocation.value == null) {
                            onLocationRetrieved(false, null)
                        } else {
                            onLocationRetrieved(true, null)
                        }
                    } else {
                        onLocationRetrieved(false, task.exception)
                    }
                }
            } else {
                onLocationRetrieved(false, null)
            }
        } catch (e: SecurityException) {
            Log.e("Refresh Last Known Location Exception: %s", e.message)
            onLocationRetrieved(false, e)
        }
    }

    fun calculateAndSortAtmsByDistance(atms: List<Atm>?, onDistancesCalculated: (Boolean, List<Atm>?) -> Unit) {
        refreshLastKnownLocation { _, _ ->
            if (lastKnownLocation.value != null) {
                distanceCalculator.calculateAndSortAtmsByDistances(atms,
                        lastKnownLocation.value, onDistancesCalculated)
            } else {
                onDistancesCalculated(false, atms)
                observeLocationChange(atms, onDistancesCalculated)
            }
        }
    }

    private fun observeLocationChange(atms: List<Atm>?, onDistancesCalculated: (Boolean, List<Atm>?) -> Unit) {
        lastKnownLocationObserver = Observer { location ->
            if (location != null) {
                distanceCalculator.calculateAndSortAtmsByDistances(atms, lastKnownLocation.value) { distanceCalculated, atms ->
                    lastKnownLocation.removeObserver(lastKnownLocationObserver)
                    onDistancesCalculated(distanceCalculated, atms)
                }
            }
        }
        lastKnownLocation.observeForever(lastKnownLocationObserver)
    }


}