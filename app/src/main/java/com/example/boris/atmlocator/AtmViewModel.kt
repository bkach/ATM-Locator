package com.example.boris.atmlocator

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.example.boris.atmlocator.repository.Atm
import com.example.boris.atmlocator.repository.AtmRepository
import com.example.boris.atmlocator.repository.Resource
import org.koin.java.standalone.KoinJavaComponent.inject

class AtmViewModel : ViewModel() {

    // Live data containing ATM data from server
    val atmsRawLiveData: MutableLiveData<List<Atm>> = MutableLiveData()

    // Live data containing final ATM data, including distance
    val atmsFinalLiveData: MutableLiveData<List<Atm>> = MutableLiveData()

    // Which ATM was selected
    val atmSelectedLiveData: MutableLiveData<Atm> = MutableLiveData()
    var distancesCalculated: Boolean = false

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    private val atmRepository: AtmRepository by inject(AtmRepository::class.java)

    init {
        loadAtms()
    }

    private fun loadAtms() {
        atmRepository.loadAtms()
                .observeForever { resource ->
                    when {
                        resource?.status == Resource.Status.SUCCESS && resource.data != null -> {
                            isLoading.value = false
                            onSuccess(resource.data!!)
                        }

                        resource?.status == Resource.Status.LOADING -> isLoading.value = true

                        else -> {
                            isLoading.value = false
                            Log.d("bdebug", "error " + resource?.message)
                        }
                    }
                }
    }

    private fun onSuccess(atms: List<Atm>) {
        Log.d("bdebug", "Loaded from repository")
        atmsRawLiveData.value = atms
    }

    fun onAtmSelected(atm: Atm?) {
        Log.d("bdebug", "Selected")
        atmSelectedLiveData.value = atm
    }

    fun onDistancesCalculated(atms: List<Atm>) {
        // Sorting on backgroudnd thread!
        Log.d("bdebug", "Distance Calculated...." + atms[0].distance)
        atmsFinalLiveData.value = atms
    }

}