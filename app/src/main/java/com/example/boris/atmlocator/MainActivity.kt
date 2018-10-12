package com.example.boris.atmlocator

import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var component: MainComponent
    private var map: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private val KEY_CAMERA_POSITION = "camera_position"
        private val KEY_LOCATION = "location"
        const val DEFAULT_ZOOM = 15.0f
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map!!.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
            super.onSaveInstanceState(outState)
        }
    }

    var lastCameraPosition : CameraPosition? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        component = DaggerMainComponent.builder()
                .mainModule(MainModule(this))
                .build()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (savedInstanceState != null) {
            lastCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
        }

        val atmViewModel = ViewModelProviders.of(this).get(AtmViewModel::class.java)
//        mainViewModel.inject(component)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.main_activity_fragment_container_0) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    var locationPermissionGranted = false

    private fun getLocationPermission() {
        /*
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            updateLocationUI()
        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
        if (locationPermissionGranted) {
            getDeviceLocationAndMoveMap()
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera
        getLocationPermission()
        updateLocationUI()
        if (lastCameraPosition != null) {
            moveCameraToLocation(lastCameraPosition!!.target)
        } else {
            getDeviceLocationAndMoveMap()
        }
    }

    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map!!.isMyLocationEnabled = true
                map!!.uiSettings.isMyLocationButtonEnabled = true
            } else {
                map!!.isMyLocationEnabled = false
                map!!.uiSettings.isMyLocationButtonEnabled = false
                lastKnownLocation = null
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }

    var lastKnownLocation : Location? = null
    val sydney = LatLng(-34.0, 151.0)


    private fun getDeviceLocationAndMoveMap() {
    /*
     * Get the best and most recent location of the device, which may be null in rare
     * cases when a location is not available.
     */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this) {task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        moveCameraToLocation(LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude))
                    } else {
                        Log.d("MainActivity", "Current location is null. Using defaults.")
                        Log.e("MainActivity", "Exception: %s", task.exception)
                        moveCameraToLocation(sydney)
                        map!!.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    private fun moveCameraToLocation(latLng: LatLng) {
        map!!.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(latLng.latitude, latLng.longitude), DEFAULT_ZOOM))
    }
}
