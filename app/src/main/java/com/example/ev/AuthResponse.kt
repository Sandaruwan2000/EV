package com.example.ev

/**
 * Represents the server's response after a successful authentication.
 */
data class AuthResponse(
    val token: String?,
    val userId: String?,
    val role: String?
)
