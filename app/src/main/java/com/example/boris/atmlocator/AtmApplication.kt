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