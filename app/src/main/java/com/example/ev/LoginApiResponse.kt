package com.example.ev

/**
 * Represents the API response from the server for the login request.
 */
data class LoginApiResponse(
    val token: String,
    val role: String,
    val userId: String,
    val expiry: String
)
