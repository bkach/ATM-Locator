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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class LocationManager {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var activity: Activity
    var locationPermissionGranted: Boolean = false
            private set
    val lastKnownLocation : MutableLiveData<Location?> = MutableLiveData()
    lateinit var lastKnownLocationObserver: Observer<Location?>
    var calculateDistancesDisposable: Disposable? = null
    val distanceCalculationLocation : Location = Location("")

    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
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
                        onLocationRetrieved(true, null)
                    } else {
                        onLocationRetrieved(false, task.exception)
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    fun calculateDistances(atms: List<Atm>?, onDistancesCalculated: (Boolean) -> Unit) {
        if (lastKnownLocation.value != null) {
            calculateDistanceInBackground(atms, onDistancesCalculated)
        } else {
            onDistancesCalculated(false)
            observeLocationChange(atms, onDistancesCalculated)
        }
    }

    private fun observeLocationChange(atms: List<Atm>?, onDistancesCalculated: (Boolean) -> Unit) {
        lastKnownLocationObserver = Observer { _ ->
            calculateDistanceInBackground(atms) {
                lastKnownLocation.removeObserver(lastKnownLocationObserver)
                onDistancesCalculated(true)
            }
        }
        lastKnownLocation.observeForever(lastKnownLocationObserver)
    }

    private fun calculateDistanceInBackground(atms: List<Atm>?, onDistancesCalculated: (Boolean) -> Unit) {
        calculateDistancesDisposable = Observable.just(atms)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext {
                            it?.forEach {
                                distanceCalculationLocation.latitude = it.latitude
                                distanceCalculationLocation.longitude = it.longitude
                                it.distance = lastKnownLocation.value?.distanceTo(distanceCalculationLocation)
                            }
                        }
                        .subscribe {
                            onDistancesCalculated(lastKnownLocation.value != null)
                        }
    }

    fun onDestroy() {
        calculateDistancesDisposable?.dispose()
    }

}