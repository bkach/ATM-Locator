@file:Suppress("unused")

package com.example.boris.atmlocator

import android.app.Application
import com.example.boris.atmlocator.repository.AtmDatabaseFactory
import com.example.boris.atmlocator.repository.AtmRepository
import com.example.boris.atmlocator.util.BackgroundTaskRunner
import org.koin.android.ext.android.startKoin
import org.koin.dsl.module.module

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
        }))
    }
}