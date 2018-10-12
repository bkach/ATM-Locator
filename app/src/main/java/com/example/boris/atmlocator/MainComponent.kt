package com.example.boris.atmlocator

import dagger.Component

@Component(modules = [MainModule::class])
interface MainComponent {
    fun inject(atmViewModel: AtmViewModel)
}
