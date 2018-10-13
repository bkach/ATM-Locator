package com.example.boris.atmlocator.repository

import retrofit2.Call
import retrofit2.http.GET

/**
 * Retrofit service used to fetch ATMs
 */
interface AtmService {

    @GET("http://207.154.210.145:8080/data/ATM_20181005_DEV.json")
    fun getAtms(): Call<List<Atm>>

}
