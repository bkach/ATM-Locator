package com.example.boris.atmlocator

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.example.boris.atmlocator.repository.Atm
import com.example.boris.atmlocator.repository.AtmRepository
import com.example.boris.atmlocator.repository.Resource
import com.example.boris.atmlocator.util.BackgroundTaskRunner
import com.example.boris.atmlocator.util.SingleLiveEvent
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

/**
 * View Model containing LiveData objects which are subscribed to by the views, namely the [MapActivity]
 * and the [com.example.boris.atmlocator.atmList.AtmListFragment]
 */
class AtmViewModel : ViewModel(), KoinComponent {

    // Atm list retrieved from the server. This contains no distance information
    val atmsRawLiveData: MutableLiveData<List<Atm>> = MutableLiveData()

    // Atm list retrieved from the view, once distance has been processed
    val atmsFinalLiveData: MutableLiveData<List<Atm>> = MutableLiveData()

    // Selected Atm. Only emits once, not once on subscribe.
    val atmSelectedLiveData: SingleLiveEvent<Atm> = SingleLiveEvent()
    val dismissError: SingleLiveEvent<Void> = SingleLiveEvent()
    val setError: SingleLiveEvent<Void> = SingleLiveEvent()

    var distancesCalculated: Boolean = false

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    private val atmRepository: AtmRepository by inject()
    private val backgroundTaskRunner: BackgroundTaskRunner
            by inject(name = AtmApplication.VIEW_MODEL_LIFECYCLE_NAME)

    init {
        loadAtms()
    }

    /**
     * Loads a list of ATMs from the repository and populates the [isLoading] LiveData and calls
     * the [onSuccess] function if succeeded
     */
    private fun loadAtms() {
        atmRepository.loadAtms()
                .observeForever { resource ->
                    when {
                        resource?.status == Resource.Status.SUCCESS && resource.data != null -> {
                            dismissError.call()
                            onSuccess(resource.data!!)
                        }

                        resource?.status == Resource.Status.LOADING -> {
                            dismissError.call()
                            isLoading.value = true
                        }

                        // TODO: Handle if there is an error in the repository!
                        else -> {
                            setError.call()
                            isLoading.value = false
                        }
                    }
                }
    }

    private fun onSuccess(atms: List<Atm>) {
        atmsRawLiveData.value = atms
    }

    fun onAtmSelected(atm: Atm?) {
        atmSelectedLiveData.value = atm
    }

    fun onDistancesCalculated(atms: List<Atm>?) {
        isLoading.value = false
        if (atms != null) {
            atmsFinalLiveData.value = atms
        }
    }

    fun onLocationPermissionAccepted() {
        atmsRawLiveData.value = atmsRawLiveData.value
    }

    override fun onCleared() {
        backgroundTaskRunner.dispose()
        super.onCleared()
    }

}