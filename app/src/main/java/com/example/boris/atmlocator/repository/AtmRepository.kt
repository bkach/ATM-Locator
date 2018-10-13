package com.example.boris.atmlocator.repository

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.util.Log
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.java.standalone.KoinJavaComponent.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Repository used to retrieve ATM atmsLiveData from either a Room database or the Network
 *
 * The policy is to retrieve the atmsLiveData ONCE and rely on the stored atmsLiveData after the atmsLiveData is retrieved
 */
class AtmRepository {

    // Final live data which emits the atm list
    val atmsLiveData: MutableLiveData<Resource<List<Atm>>> = MutableLiveData()

    private val atmDatabaseFactory: AtmDatabaseFactory by inject(AtmDatabaseFactory::class.java)
    private val atmDatabase: AtmDatabase = atmDatabaseFactory.getDatabase()
    private lateinit var databaseObserver: Observer<List<Atm>>

    private val atmRetrofitService =
            Retrofit.Builder()
                    .baseUrl(ATM_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create<AtmService>(AtmService::class.java)

    companion object {
        // TODO: Ideally, this would be stored in a more secure way
        const val ATM_URL: String = "http://207.154.210.145:8080/"
    }

    fun loadAtms(): LiveData<Resource<List<Atm>>> {
        atmsLiveData.value = Resource.loading(null)
        attemptToLoadAtmsFromDatabase()
        return atmsLiveData
    }

    private fun attemptToLoadAtmsFromDatabase() {
        val loadDatabaseData = atmDatabase.atmDao().load()
        createDatabaseObserver(loadDatabaseData)
        loadDatabaseData.observeForever(databaseObserver)
    }

    /**
     * In order to be able to unsubscribe, the observer needs a reference to itself.
     *
     * By creating an observer each time and storing it in the class, a reference is kept
     * which can later be unsubscribed.
     */
    private fun createDatabaseObserver(loadDatabaseData: LiveData<List<Atm>>) {
        databaseObserver = Observer { atms ->
            if (atms == null || atms.isEmpty()) {
                Log.d("bdebug", "db miss loading from network...")
                loadAtmsFromNetwork()
            } else {
                Log.d("bdebug", "loading from db...")
                atmsLiveData.value = Resource.success(atms)
            }
            loadDatabaseData.removeObserver(databaseObserver)
        }
    }

    private fun loadAtmsFromNetwork() {
        atmRetrofitService.getAtms().enqueue(object : Callback<List<Atm>> {
            @SuppressLint("CheckResult")
            override fun onResponse(call: Call<List<Atm>>, response: Response<List<Atm>>) {
                if (!response.isSuccessful || response.body() == null) {
                    atmsLiveData.value = Resource.error("Unsuccessful response", null)
                } else {
                    Completable.fromAction {
                        Log.d("bdebug", "saving dao on " +  Thread.currentThread().getName())
                        atmDatabase.atmDao().deleteAll()
                        atmDatabase.atmDao().save(response.body()!!)
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        atmsLiveData.value = Resource.success(response.body()!!)
                    }
                }
            }

            override fun onFailure(call: Call<List<Atm>>, t: Throwable) {
                atmsLiveData.value = Resource.error(t.message!!, null)
            }
        })
    }
}