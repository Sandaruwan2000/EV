package com.example.ev

/**
 * Represents the top-level API response from the server for the login request.
 * It wraps the actual authentication data in a nested object.
 */
data class LoginApiResponse(
    val success: Boolean,
    val data: AuthResponse, // The nested object containing auth details
    val message: String
)
