package com.example.ev

/**
 * Represents the data sent to the server when creating a new booking.
 */
data class Booking(
    val chargingStationId: String,
    val ownerNIC: String,
    val slotNumber: Int,
    val reservationDateTime: String,
    val vehicleType: String,
    val vehicleNumber: String,
    val startTime: String,
    val endTime: String
)
