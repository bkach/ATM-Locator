package com.example.boris.atmlocator.repository

import retrofit2.Call
import retrofit2.http.GET

interface AtmService {

    @GET("atmsLiveData/ATM_20181005_DEV.json")
    fun getAtms(): Call<List<Atm>>

}
