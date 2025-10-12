package com.example.ev

/**
 * Represents the data sent to the server when updating a user's profile.
 */
data class UpdateProfileRequest(
    val fullName: String,
    val nic: String,
    val email: String
)
