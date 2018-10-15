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

package com.example.boris.atmlocator.util

import android.location.Location
import com.example.boris.atmlocator.AtmApplication
import com.example.boris.atmlocator.repository.Atm
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

/**
 * Class uses to calculate the distances for all ATMs in a [List] of [Atm]s and sort them by
 * distance in the background
 */
class DistanceCalculator : KoinComponent {
    private val distanceCalculationLocation : Location = Location("")
    private val backgroundTaskRunner: BackgroundTaskRunner
            by inject(name = AtmApplication.ACTIVITY_LIFECYCLE_NAME)

    fun calculateAndSortAtmsByDistances(atms: List<Atm>?, location: Location?,
                                        onDistancesCalculated: (Boolean, List<Atm>?) -> Unit) {
        var returnedAtms: List<Atm>? = listOf()
        backgroundTaskRunner.run({
            val atmsMutable = atms?.toMutableList()
            atmsMutable?.map {
                distanceCalculationLocation.latitude = it.latitude
                distanceCalculationLocation.longitude = it.longitude
                it.distance = location?.distanceTo(distanceCalculationLocation)
                it
            }
            if (atmsMutable?.get(0)?.distance != null) {
                atmsMutable.sortBy { closest(it) }
            }
            returnedAtms = atmsMutable
        }, {
            onDistancesCalculated(true, returnedAtms)
        })
    }

    private fun closest(atm: Atm): Float = atm.distance!!
}