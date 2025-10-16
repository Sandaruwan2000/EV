package com.example.ev

/**
 * Represents the top-level response from the /api/allBookings/station/{id} endpoint.
 * This class correctly models the JSON structure by reusing the existing BookingDetails class.
 */
data class StationBookingsResponse(val data: List<BookingDetails>)
