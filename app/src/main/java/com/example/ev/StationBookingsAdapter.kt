package com.example.ev

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class StationBookingsAdapter(
    private var bookings: List<BookingDetails>,
    private val onItemClicked: (BookingDetails) -> Unit // Add a click listener lambda
) : 
    RecyclerView.Adapter<StationBookingsAdapter.BookingViewHolder>() {

    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val outputFormat = SimpleDateFormat("MMM d, yyyy, hh:mm a", Locale.getDefault())

    fun updateBookings(newBookings: List<BookingDetails>) {
        this.bookings = newBookings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_station_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.bind(booking)
        // Set the click listener on the item view
        holder.itemView.setOnClickListener { onItemClicked(booking) }
    }

    override fun getItemCount() = bookings.size

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idTextView: TextView = itemView.findViewById(R.id.bookingIdTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        private val vehicleNumberTextView: TextView = itemView.findViewById(R.id.vehicleNumberTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.bookingTimeTextView)

        fun bind(booking: BookingDetails) {
            idTextView.text = "Booking ID: ${booking.id}"
            statusTextView.text = "Status: ${booking.status}"
            vehicleNumberTextView.text = "Vehicle: ${booking.vehicleNumber}"

            val timeStr = booking.startTime ?: booking.reservationDateTime
            if (timeStr != null && !timeStr.startsWith("0001")) {
                try {
                    val date = inputFormat.parse(timeStr)
                    timeTextView.text = if (date != null) outputFormat.format(date) else "Invalid Time"
                } catch (e: Exception) {
                    timeTextView.text = "Invalid Time Format"
                }
            } else {
                timeTextView.text = "Time Not Available"
            }
        }
    }
}