package com.example.ev

/**
 * Represents the data for a single charging station, including its schedules.
 */
data class ChargingStation(
    val id: String,
    val name: String,
    val location: String,
    val type: String,
    val availableSlots: Int,
    val isActive: Boolean,
    val schedules: List<Schedule>
)
