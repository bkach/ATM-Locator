package com.example.boris.atmlocator

import android.content.res.Resources
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapManager : OnMapReadyCallback {

    private var map: GoogleMap? = null
    private lateinit var onMapInit: () -> Unit
    var lastCameraPosition : CameraPosition? = null

    companion object {
        private val KEY_CAMERA_POSITION = "camera_position"
        const val DEFAULT_ZOOM = 15.0f
    }

    fun restoreInstanceState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            lastCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
    }

    fun init(activity: AppCompatActivity, onMapInit: () -> Unit) {
        this.onMapInit = onMapInit
        val mapFragment = activity.supportFragmentManager
                .findFragmentById(R.id.main_activity_fragment_container_0) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        onMapInit()
    }

    fun updateMapLocationUI(locationPermissionGranted: Boolean) {
        if (map != null) {
            try {
                if (locationPermissionGranted) {
                    map!!.isMyLocationEnabled = true
                    map!!.uiSettings.isMyLocationButtonEnabled = true
                } else {
                    map!!.isMyLocationEnabled = false
                    map!!.uiSettings.isMyLocationButtonEnabled = false
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message)
            }
        }
    }

    fun moveCameraToLocation(latLng: LatLng) {
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                LatLng(latLng.latitude, latLng.longitude), DEFAULT_ZOOM))
    }

    fun saveInstanceState(outState: Bundle) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map!!.cameraPosition)
        }
    }

    var markers: HashMap<Atm, Marker?> = HashMap()

    fun addMarkers(atms: List<Atm>?) {
        map?.clear()
        markers.clear()

        atms?.forEach { atm ->
            markers[atm] = map?.addMarker(MarkerOptions()
                    .position(LatLng(atm.latitude, atm.longitude))
                    .title(atm.name)
                    .snippet(atm.address.formatted))

        }
    }

    fun moveTo(selectedAtm: Atm?) {
        val selectedMarker: Marker? = markers[selectedAtm]
        if (selectedMarker != null) {
            map?.animateCamera(CameraUpdateFactory.newCameraPosition(
                    CameraPosition.builder().target(selectedMarker.position).zoom(DEFAULT_ZOOM).build()))
            selectedMarker.showInfoWindow()
        }
    }

}