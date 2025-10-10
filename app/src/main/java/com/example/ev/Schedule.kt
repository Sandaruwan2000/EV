package com.example.ev

data class Schedule(
    val slotNumber: Int,
    val startTime: String,
    val endTime: String,
    val isAvailable: Boolean
)
