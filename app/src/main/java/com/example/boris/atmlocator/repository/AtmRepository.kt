package com.example.boris.atmlocator.repository

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.arch.persistence.room.Room
import android.content.Context
import android.util.Log
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.java.standalone.KoinJavaComponent.inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AtmRepository(baseContext: Context) {

    val data: MutableLiveData<Resource<List<Atm>>> = MutableLiveData()
    private val atmService: AtmService

    val context: Context by inject(Context::class.java)

    private val atmDatabase: AtmDatabase =
            Room.databaseBuilder(context, AtmDatabase::class.java, "atm-db").build()


    companion object {
        const val ATM_URL: String = "http://207.154.210.145:8080/"
    }

    init {
        atmService = Retrofit.Builder()
                .baseUrl(ATM_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create<AtmService>(AtmService::class.java)
    }

    fun loadAtms(): LiveData<Resource<List<Atm>>> {
        data.value = Resource.loading(null)

        attemptToLoadAtmsFromDatabase()

        return data
    }

    private lateinit var databaseObserver: Observer<List<Atm>>

    private fun attemptToLoadAtmsFromDatabase() {
        val loadDatabaseData = atmDatabase.atmDao().load()

        databaseObserver = Observer { atms ->
            if (atms == null || atms.isEmpty()) {
                Log.d("bdebug", "db miss loading from network...")
                loadAtmsFromNetwork()
            } else {
                Log.d("bdebug", "loading from db...")
                data.value = Resource.success(atms)
            }
            unsubscribeObserver(loadDatabaseData)
        }

        loadDatabaseData.observeForever(databaseObserver)
    }

    private fun unsubscribeObserver(loadDatabaseData: LiveData<List<Atm>>) {
        loadDatabaseData.removeObserver(databaseObserver)
    }

    private fun loadAtmsFromNetwork() {
        atmService.getAtms().enqueue(object : Callback<List<Atm>> {
            @SuppressLint("CheckResult")
            override fun onResponse(call: Call<List<Atm>>, response: Response<List<Atm>>) {
                if (!response.isSuccessful || response.body() == null) {
                    data.value = Resource.error("Unsuccessful response", null)
                } else {
                    Completable.fromAction {
                        Log.d("bdebug", "saving dao on " +  Thread.currentThread().getName())
                        atmDatabase.atmDao().deleteAll()
                        atmDatabase.atmDao().save(response.body()!!)
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        data.value = Resource.success(response.body()!!)
                    }
                }
            }

            override fun onFailure(call: Call<List<Atm>>, t: Throwable) {
                data.value = Resource.error(t.message!!, null)
            }
        })
    }
}