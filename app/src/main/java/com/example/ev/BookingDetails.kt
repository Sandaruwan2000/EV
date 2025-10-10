package com.example.ev

data class BookingDetails(
    val id: String,
    val chargingStationId: String,
    val evOwnerUserId: String?,
    val ownerNIC: String?,
    val slotNumber: Int,
    val reservationDateTime: String,
    val createdAt: String,
    val status: String
)
