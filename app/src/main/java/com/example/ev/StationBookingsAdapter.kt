package com.example.ev

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class StationBookingsAdapter(private var bookings: List<BookingDetails>) : 
    RecyclerView.Adapter<StationBookingsAdapter.BookingViewHolder>() {

    // Define the date format for parsing UTC and formatting to local time
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun updateBookings(newBookings: List<BookingDetails>) {
        this.bookings = newBookings
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(bookings[position])
    }

    override fun getItemCount() = bookings.size

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idTextView: TextView = itemView.findViewById(R.id.bookingIdTextView)
        private val timeTextView: TextView = itemView.findViewById(R.id.bookingTimeTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.bookingStatusTextView)

        fun bind(booking: BookingDetails) {
            idTextView.text = "Booking ID: ${booking.id}"
            statusTextView.text = "Status: ${booking.status}"

            // Use the correct time field from BookingDetails and format it
            val timeStr = booking.startTime ?: booking.startTime
            if (timeStr != null && !timeStr.startsWith("0001")) { // Ignore default/invalid dates
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