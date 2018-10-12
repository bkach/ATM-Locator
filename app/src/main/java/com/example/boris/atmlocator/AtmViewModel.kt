package com.example.boris.atmlocator

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class AtmViewModel : ViewModel() {

    val atmsLiveData: MutableLiveData<List<Atm>> = MutableLiveData()
    val atmSelectedLiveData: MutableLiveData<Atm> = MutableLiveData()
    var distancesCalculated: Boolean = false

    init {
        atmsLiveData.value = listOf(
                Atm("Place1", 55.5969821, 12.9925668, Address("Helmfeltsgatan 10")),
                Atm("Place2", 55.59783, 12.99216, Address("Helmfeltsgatan 11"))
        )
    }

    fun onAtmSelected(atm: Atm?) {
        atmSelectedLiveData.value = atm
    }

}