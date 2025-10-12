package com.example.ev

/**
 * Represents the user profile data returned by the server.
 */
data class UserProfileResponse(
    val id: String,
    val username: String,
    val nic: String,
    val fullName: String,
    val email: String,
    val isActive: Boolean,
    val createdAt: String
)
