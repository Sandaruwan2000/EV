package com.example.ev

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/Users")
    fun registerUser(@Body user: UserRegistration): Call<Unit>

    @POST("api/Users/authenticate")
    fun authenticate(@Body authRequest: AuthRequest): Call<LoginApiResponse>

    @GET("api/ChargingStation")
    fun getChargingStations(): Call<List<ChargingStation>>

    @GET("api/ChargingStation/{id}")
    fun getStationDetails(@Path("id") stationId: String): Call<ChargingStation>

    @POST("api/Booking")
    fun bookStation(@Body booking: Booking): Call<BookingDetails>

    @PUT("api/Booking/{id}")
    fun updateBooking(@Path("id") bookingId: String, @Body booking: Booking): Call<BookingDetails>

    @DELETE("api/Booking/{id}")
    fun cancelBooking(@Path("id") bookingId: String): Call<Unit>

    @GET("api/Booking")
    fun getAllBookings(): Call<List<BookingDetails>>

    @GET("api/Users/evowner/profile/{id}")
    fun getUserDetails(@Path("id") userId: String): Call<UserProfileResponse>

    @PUT("api/Users/evowner/profile/{id}")
    fun updateUserProfile(@Path("id") userId: String, @Body profile: UpdateProfileRequest): Call<Unit>

    @DELETE("api/Users/evowner/profile/{id}")
    fun deactivateUser(@Path("id") userId: String): Call<Unit>
}
