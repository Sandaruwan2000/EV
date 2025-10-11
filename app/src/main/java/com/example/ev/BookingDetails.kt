package com.example.ev

/**
 * Represents the detailed information of a booking retrieved from the server.
 */
data class BookingDetails(
    val id: String,
    val chargingStationId: String,
    val ownerNIC: String?,
    val slotNumber: Int,
    val reservationDateTime: String,
    val vehicleType: String?,
    val vehicleNumber: String?,
    val startTime: String?, // Can be null
    val endTime: String?,   // Can be null
    val slotsBooked: Int,
    val createdAt: String,
    val status: String
)
