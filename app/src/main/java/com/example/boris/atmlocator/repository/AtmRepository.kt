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

package com.example.boris.atmlocator.repository

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.example.boris.atmlocator.AtmApplication
import com.example.boris.atmlocator.util.BackgroundTaskRunner
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Repository used to retrieve ATM atmsLiveData from either a Room database or the Network
 *
 * The policy is to retrieve the atmsLiveData ONCE and rely on the stored atmsLiveData after the atmsLiveData is retrieved
 */
class AtmRepository : KoinComponent {

    // Final live data which emits the atm list
    val atmsLiveData: MutableLiveData<Resource<List<Atm>>> = MutableLiveData()

    // A Database factory is injected because it uses context, something which would not be be ideal in a mocked class
    private val atmDatabaseFactory: AtmDatabaseFactory by inject()
    private val backgroundTaskRunner: BackgroundTaskRunner by inject(name = AtmApplication.VIEW_MODEL_LIFECYCLE_NAME)
    private val atmRetrofitService: AtmService by inject()

    private val atmDatabase: AtmDatabase = atmDatabaseFactory.getDatabase()
    private lateinit var databaseObserver: Observer<List<Atm>>


    companion object {
        // TODO: Ideally, this would be stored in a more secure way
        const val ATM_URL: String = "http://207.154.210.145:8080/"
    }

    fun loadAtms(): LiveData<Resource<List<Atm>>> {
        atmsLiveData.value = Resource.loading(null)
        attemptToLoadAtmsFromDatabase()
        return atmsLiveData
    }

    private fun attemptToLoadAtmsFromDatabase() {
        val loadDatabaseData = atmDatabase.atmDao().load()
        createDatabaseObserver(loadDatabaseData)
        loadDatabaseData.observeForever(databaseObserver)
    }

    /**
     * Creates a database observer which checks if the database has any values, and if not
     * attempts to populate the database from the network
     *
     *
     * In order to be able to unsubscribe, the observer needs a reference to itself.
     *
     * By creating an observer each time and storing it in the class, a reference is kept
     * which can later be unsubscribed.
     */
    private fun createDatabaseObserver(loadDatabaseData: LiveData<List<Atm>>) {
        databaseObserver = Observer { atms ->
            if (atms == null || atms.isEmpty()) {
                loadAtmsFromNetwork()
            } else {
                atmsLiveData.value = Resource.success(atms)
            }
            loadDatabaseData.removeObserver(databaseObserver)
        }
    }

    /**
     * Loads ATMs from the Network
     *
     * If successful, saves the ATMs in the Room database
     */
    private fun loadAtmsFromNetwork() {
        atmRetrofitService.getAtms().enqueue(object : Callback<List<Atm>> {
            override fun onResponse(call: Call<List<Atm>>, response: Response<List<Atm>>) {
                if (!response.isSuccessful || response.body() == null) {
                    atmsLiveData.value = Resource.error("Unsuccessful response " + response.errorBody()?.string(), null)
                } else {
                    backgroundTaskRunner.run({
                        // All database processes must be run in the background
                        atmDatabase.atmDao().deleteAll()
                        atmDatabase.atmDao().save(response.body()!!)
                    }, {
                        atmsLiveData.value = Resource.success(response.body()!!)
                    })
                }
            }

            override fun onFailure(call: Call<List<Atm>>, t: Throwable) {
                atmsLiveData.value = Resource.error(t.message!!, null)
            }
        })
    }
}