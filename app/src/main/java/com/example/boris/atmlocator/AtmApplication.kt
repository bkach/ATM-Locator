package com.example.boris.atmlocator

import android.app.Application
import com.example.boris.atmlocator.repository.AtmDatabaseFactory
import com.example.boris.atmlocator.repository.AtmRepository
import org.koin.android.ext.android.startKoin
import org.koin.dsl.module.module

class AtmApplication: Application() {
    override fun onCreate(){
        super.onCreate()
        startKoin(this, listOf(module {
            single { AtmDatabaseFactory() }
            single { AtmRepository() }
        }))
    }
}