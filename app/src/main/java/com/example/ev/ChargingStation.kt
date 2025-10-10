package com.example.ev

data class ChargingStation(
    val id: String,
    val name: String,
    val location: String,
    val type: String,
    val availableSlots: Int,
    val isActive: Boolean,
    val schedules: List<Schedule>,
    val ownerUserId: String,
    val ownerNIC: String?,
    val imageUrl: String? = null
)
