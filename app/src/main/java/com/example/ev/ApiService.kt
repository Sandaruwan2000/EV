package com.example.ev

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    @POST("api/Users")
    fun registerUser(@Body user: UserRegistration): Call<Unit>

    @POST("api/Users/authenticate")
    fun authenticate(@Body authRequest: AuthRequest): Call<LoginApiResponse>

    @GET("api/Booking/user/{userId}/bookings")
    fun getUserBookings(@Path("userId") userId: String): Call<List<BookingDetails>>

    // EV Owner Specific
    @GET("api/Users/evowner/profile/{id}")
    fun getEvOwnerUserDetails(@Path("id") userId: String): Call<UserProfileResponse>

    @PUT("api/Users/evowner/profile/{id}")
    fun updateUserProfile(@Path("id") userId: String, @Body profile: UpdateProfileRequest): Call<Unit>

    @DELETE("api/Users/evowner/profile/{id}")
    fun deactivateUser(@Path("id") userId: String): Call<Unit>

    // Backoffice & General User
    @GET("api/Users/{id}")
    fun getUserDetails(@Path("id") userId: String): Call<User>

    // Charging Station
    @GET("api/ChargingStation")
    fun getChargingStations(): Call<List<ChargingStation>>

    @GET("api/ChargingStation/{id}")
    fun getStationDetails(@Path("id") stationId: String): Call<ChargingStation>

    // Booking
    @POST("api/Booking")
    fun bookStation(@Body booking: Booking): Call<BookingDetails>

    @GET("api/allBookings/station/{stationId}")
    fun getBookingsForStation(@Path("stationId") stationId: String): Call<StationBookingsResponse>

    @PUT("api/Booking/{id}")
    fun updateBooking(@Path("id") bookingId: String, @Body booking: Booking): Call<BookingDetails>

    @PATCH("api/Booking/{id}/cancel")
    fun cancelBooking(@Path("id") bookingId: String): Call<Unit>

    @PUT("api/Booking/{id}/status")
    fun updateBookingStatus(@Path("id") bookingId: String, @Body statusUpdate: BookingStatusUpdateRequest): Call<Unit>

    @GET("api/Booking")
    fun getAllBookings(): Call<List<BookingDetails>>
}
