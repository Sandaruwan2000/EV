package com.example.ev

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/Users")
    fun registerUser(@Body user: UserRegistration): Call<Unit>

    @POST("api/Users/authenticate")
    fun authenticate(@Body authRequest: AuthRequest): Call<AuthResponse>

    @GET("api/ChargingStation")
    fun getChargingStations(): Call<List<ChargingStation>>

    @POST("api/Booking")
    fun bookStation(@Body booking: Booking): Call<Unit>

    @GET("api/Booking")
    fun getAllBookings(): Call<List<BookingDetails>>
}
