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

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.Snackbar.LENGTH_INDEFINITE
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.example.boris.atmlocator.LocationManager.Companion.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
import com.example.boris.atmlocator.repository.Atm
import com.example.boris.atmlocator.util.BackgroundTaskRunner
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.ext.android.inject

/**
 * Main Activity in the app. Its main responsibilities are:
 *
 * - Communicating with and observing the [AtmViewModel]
 * - Communicating with the [LocationManager]
 * - Communicating with the [MapManager] which contains the map in the class' [MapFragment]
 */
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
            observeErrors()
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

    var snackbar: Snackbar? = null

    private fun observeErrors() {
        atmViewModel.setError.observe(this, Observer {
            showError()
        })

        atmViewModel.dismissError.observe(this, Observer {
            snackbar?.dismiss()
        })
    }

    private fun showError() {
        snackbar = Snackbar.make(activity_parent_constraint_layout, "Error getting data, please try again later", LENGTH_INDEFINITE)
        snackbar?.show()
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
        mapManager.clearMarkers()
        super.onDestroy()
    }

}
