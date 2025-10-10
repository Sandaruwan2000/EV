package com.example.ev

data class UserRegistration(
    val username: String,
    val passwordHash: String,
    val role: String,
    val isActive: Boolean,
    val nic: String,
    val fullName: String,
    val email: String
)
