package com.example.boris.atmlocator

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class AtmViewModel : ViewModel() {

    // Live data containing ATM data from server
    val atmsRawLiveData: MutableLiveData<List<Atm>> = MutableLiveData()

    // Live data containing final ATM data, including distance
    val atmsFinalLiveData: MutableLiveData<List<Atm>> = MutableLiveData()

    // Which ATM was selected
    val atmSelectedLiveData: MutableLiveData<Atm> = MutableLiveData()

    init {
        atmsRawLiveData.value = listOf(
                Atm("Place1", 55.5969821, 12.9925668, Address("Helmfeltsgatan 10")),
                Atm("Place2", 55.59783, 12.99216, Address("Helmfeltsgatan 11"))
        )
    }

    fun onAtmSelected(atm: Atm?) {
        atmSelectedLiveData.value = atm
    }

    fun onDistancesCalculated(atms: List<Atm>?) {
        atmsFinalLiveData.value = atms
    }

}