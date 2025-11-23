package com.example.ev

/**
 * Represents the top-level response from the /api/AllBookings/station/{id} endpoint.
 * This class correctly models the JSON structure: { "data": [...] }
 * by reusing the app's existing BookingDetails class.
 */
data class StationBookingsResponse(val data: List<BookingDetails>)
