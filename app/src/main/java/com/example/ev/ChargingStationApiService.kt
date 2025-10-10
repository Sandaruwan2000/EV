package com.example.ev

import retrofit2.Call
import retrofit2.http.GET

interface ChargingStationApiService {
    @GET("api/ChargingStation")
    fun getChargingStations(): Call<List<ChargingStation>>
}