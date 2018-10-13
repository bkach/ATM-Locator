package com.example.boris.atmlocator

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.boris.atmlocator.LocationManager.Companion.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.example.boris.atmlocator.repository.Atm
import com.google.android.gms.maps.model.LatLng


class MapActivity : AppCompatActivity() {

    private val locationManager = LocationManager()
    private val mapManager = MapManager()
    lateinit var atmViewModel: AtmViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        atmViewModel = ViewModelProviders.of(this).get(AtmViewModel::class.java)
        initView()
        restoreInstanceState(savedInstanceState)
    }

    private fun initView() {
        locationManager.init(this)
        mapManager.init(this) {
            locationManager.getLocationPermission {
                mapManager.updateMapLocationUI(locationManager.locationPermissionGranted)
            }
            moveCameraToInitialPosition()
            observeRawViewModelAtmData()
            observeFinalViewModelAtmData()
            observeViewModelSelectedAtm()
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            mapManager.restoreInstanceState(savedInstanceState)
            locationManager.restoreInstanceState(savedInstanceState)
        }
    }

    private fun observeRawViewModelAtmData() {
        atmViewModel.atmsRawLiveData.observe(this, Observer { atms ->
            locationManager.calculateDistances(atms) { isDistanceCalculated, atmsResult ->
                atmViewModel.distancesCalculated = isDistanceCalculated
                atmViewModel.onDistancesCalculated(atmsResult)
            }
        })
    }

    private fun observeFinalViewModelAtmData() {
        atmViewModel.atmsFinalLiveData.observe(this, Observer { atms ->
            mapManager.addMarkers(atms)
        })
    }

    private fun observeViewModelSelectedAtm() {
        atmViewModel.atmSelectedLiveData.observe(this, Observer { selectedAtm ->
            launchDialog(selectedAtm) {
                mapManager.moveTo(selectedAtm)
            }
        })
    }

    private fun launchDialog(selectedAtm: Atm?, onGoToMap: () -> Unit) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.dialog_message, selectedAtm?.name))
        builder.setPositiveButton(R.string.move_to_location) { _, _ -> onGoToMap() }
        builder.setNegativeButton(R.string.cancel) { _, _ -> }
        builder.setCancelable(true)
        builder.create().show()
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationManager.onLocationPermissionResult(false)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationManager.onLocationPermissionResult(true)
                    atmViewModel.onLocationPermissionAccepted()
                }
            }
        }

        mapManager.updateMapLocationUI(locationManager.locationPermissionGranted)
    }

    private fun moveCameraToInitialPosition() {
        if (mapManager.lastCameraPosition != null) {
            mapManager.moveCameraToLocation(mapManager.lastCameraPosition!!.target)
        } else {
            moveMapToDeviceLocation()
        }
    }

    private fun moveMapToDeviceLocation() {
        locationManager.refreshLastKnownLocation { success, exception ->
            if (success) {
                mapManager.moveCameraToLocation(
                        LatLng(locationManager.lastKnownLocation.value!!.latitude,
                               locationManager.lastKnownLocation.value!!.longitude))
            } else {
                Log.d("MapActivity", "Current location is null. Using defaults.")
                Log.e("MapActivity", "Exception: %s", exception)
                mapManager.moveCameraToLocation(MapManager.DEFAULT_LOCATION)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        mapManager.saveInstanceState(outState)
        locationManager.saveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        locationManager.onDestroy()
        super.onDestroy()
    }
}
