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

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import com.example.boris.atmlocator.repository.*
import com.example.boris.atmlocator.util.BackgroundTaskRunner
import com.nhaarman.mockitokotlin2.*
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.mockito.junit.MockitoJUnit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AtmRepositoryTests {

    @Rule
    @JvmField
    var mockitoRule = MockitoJUnit.rule()!!

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mockBackgroundTaskRunner = MockBackgroundTaskRunner()

    private val retrofitService = mock<AtmService>()
    private val databaseFactory = mock<AtmDatabaseFactory>()
    private val database = mock<AtmDatabase>()
    private val dao = mock<AtmDao>()
    private val retrofitCall = mock<Call<List<Atm>>>()
    private val mockAtm = mock<Atm>()

    private val databaseLiveData = MutableLiveData<List<Atm>>()

    private lateinit var atmRepository: AtmRepository

    @Before
    fun setup() {
        setupMockDependencies()
        atmRepository = AtmRepository()
    }

    @After
    fun after() {
        stopKoin()
    }

    private fun setupMockDependencies() {
        whenever(databaseFactory.getDatabase()).thenReturn(database)
        whenever(database.atmDao()).thenReturn(dao)
        whenever(dao.load()).thenReturn(databaseLiveData)
        whenever(retrofitService.getAtms()).thenReturn(retrofitCall)

        startKoin(listOf(
                module {
                    single(AtmApplication.VIEW_MODEL_LIFECYCLE_NAME) {
                        mockBackgroundTaskRunner as BackgroundTaskRunner }
                    single { retrofitService }
                    single { databaseFactory }
                }
        ))
    }

    @Test
    fun `when loading ATMs, the resource should be set to loading`() {
        atmRepository.loadAtms()
        assertTrue(atmRepository.atmsLiveData.value!!.status == Resource.Status.LOADING)
    }

    @Test
    fun `when loading ATMs, and the database is not empty, set value as success`() {
        atmRepository.loadAtms()
        databaseLiveData.value = listOf(mockAtm)
        atmRepository.atmsLiveData.observeForever {
            assertEquals(it?.status, Resource.Status.SUCCESS)
            assertEquals(it?.data, listOf(mockAtm))
        }
    }

    @Test
    fun `when loading ATMs, and the database is empty, load atms from the network`() {
        atmRepository.loadAtms()
        databaseLiveData.value = listOf()
        captureRetrofitCall()
        verify(retrofitService).getAtms()
    }

    @Test
    fun `when loading ATMs from the network and a successful response is reached save values to the database`() {
        atmRepository.loadAtms()
        databaseLiveData.value = listOf()
        val call = captureRetrofitCall()
        call.onResponse(mock<Call<List<Atm>>>(), Response.success(listOf(mockAtm)))
        mockBackgroundTaskRunner.backgroundTask?.invoke()
        verify(dao).deleteAll()
        verify(dao).save(eq(listOf(mockAtm)))
    }

    @Test
    fun `when loading ATMs from the network and a successful response is reached save values send successful data to callback`() {
        atmRepository.loadAtms()
        databaseLiveData.value = listOf()
        val call = captureRetrofitCall()
        call.onResponse(mock<Call<List<Atm>>>(), Response.success(listOf(mockAtm)))
        mockBackgroundTaskRunner.mainThreadTask?.invoke()
        atmRepository.atmsLiveData.observeForever {
            assertEquals(it?.status, Resource.Status.SUCCESS)
            assertEquals(it?.data, listOf(mockAtm))
        }
    }

    @Test
    fun `when loading ATMs from the network and a response fails, send an the error to the callback`() {
        atmRepository.loadAtms()
        databaseLiveData.value = listOf()
        val call = captureRetrofitCall()
        call.onResponse(mock<Call<List<Atm>>>(), Response.error(404,
                ResponseBody.create(null, "Error")))
        mockBackgroundTaskRunner.mainThreadTask?.invoke()
        atmRepository.atmsLiveData.observeForever {
            assertEquals(it?.status, Resource.Status.ERROR)
        }
    }

    private fun captureRetrofitCall() : Callback<List<Atm>> {
        val retrofitCallArgumentCaptor = argumentCaptor<Callback<List<Atm>>>()
        verify(retrofitCall).enqueue(retrofitCallArgumentCaptor.capture())
        return retrofitCallArgumentCaptor.firstValue
    }

}
