package com.example.boris.atmlocator

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.boris.atmlocator.LocationManager.Companion.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.google.android.gms.maps.model.LatLng


class MainActivity : AppCompatActivity() {

    lateinit var component: MainComponent
    private val locationManager = LocationManager()
    private val mapManager = MapManager()
    lateinit var atmViewModel: AtmViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        component = DaggerMainComponent.builder()
                .mainModule(MainModule(this))
                .build()


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
            locationManager.calculateDistances(atms) { atmsResult ->
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
            mapManager.moveTo(selectedAtm)
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationManager.onLocationPermissionResult(false)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationManager.onLocationPermissionResult(true)
                }
            }
        }

        mapManager.updateMapLocationUI(locationManager.locationPermissionGranted)
        if (locationManager.locationPermissionGranted) {
            moveMapToDeviceLocation()
        }
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
                Log.d("MainActivity", "Current location is null. Using defaults.")
                Log.e("MainActivity", "Exception: %s", exception)
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
