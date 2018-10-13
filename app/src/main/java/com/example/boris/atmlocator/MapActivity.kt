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
import com.example.boris.atmlocator.util.BackgroundTaskRunner
import com.google.android.gms.maps.model.LatLng
import org.koin.android.ext.android.inject

class MapActivity : AppCompatActivity() {

    private val locationManager = LocationManager()
    private val mapManager = MapManager()
    private lateinit var atmViewModel: AtmViewModel

    private val backgroundTaskRunner: BackgroundTaskRunner
            by inject(name = AtmApplication.ACTIVITY_LIFECYCLE_NAME)

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
            // TODO: Location changes? What should the behavior be when the location is turned on and off?
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
            locationManager.calculateAndSortAtmsByDistance(atms) { isDistanceCalculated, atmsResult ->
                atmViewModel.distancesCalculated = isDistanceCalculated
                // TODO: If distance not calculated, hide distance?
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
        Log.d("bdebug", "request permission result")
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    locationManager.onLocationPermissionResult(true)
                    atmViewModel.onLocationPermissionAccepted()
                    moveMapToDeviceLocation()
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
        backgroundTaskRunner.dispose()
        super.onDestroy()
    }

}
