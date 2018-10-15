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

@file:Suppress("unused")

package com.example.boris.atmlocator

import android.app.Application
import com.example.boris.atmlocator.repository.AtmDatabaseFactory
import com.example.boris.atmlocator.repository.AtmRepository
import com.example.boris.atmlocator.repository.AtmRepository.Companion.ATM_URL
import com.example.boris.atmlocator.repository.AtmService
import com.example.boris.atmlocator.util.BackgroundTaskRunner
import org.koin.android.ext.android.startKoin
import org.koin.dsl.module.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Application class used for dependency injection using Koin
 */
class AtmApplication: Application() {

    companion object {
        /**
         * Each architectural layer needs its own task runner, as they have separate lifecycles
         */
        const val VIEW_MODEL_LIFECYCLE_NAME = "ViewModel"
        const val ACTIVITY_LIFECYCLE_NAME = "Activity"
    }

    override fun onCreate(){
        super.onCreate()
        startKoin(this, listOf(module {
            single(VIEW_MODEL_LIFECYCLE_NAME) { BackgroundTaskRunner() }
            single(ACTIVITY_LIFECYCLE_NAME) { BackgroundTaskRunner() }
            single { AtmDatabaseFactory() }
            single { AtmRepository() }
            single {
                Retrofit.Builder()
                        .baseUrl(ATM_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build().create<AtmService>(AtmService::class.java)
            }
        }))
    }
}