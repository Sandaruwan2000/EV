package com.example.ev

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookingsAdapter(private var bookings: List<BookingDetails>) : RecyclerView.Adapter<BookingsAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_item, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.stationIdTextView.text = "Station ID: ${booking.chargingStationId}"
        holder.statusTextView.text = "Status: ${booking.status}"
        holder.reservationTimeTextView.text = "Reservation: ${booking.reservationDateTime}"

        // Set a default image for the booking
        holder.bookingImageView.setImageResource(R.drawable.placeholder_image)
    }

    override fun getItemCount() = bookings.size

    fun updateBookings(newBookings: List<BookingDetails>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookingImageView: ImageView = itemView.findViewById(R.id.bookingImageView)
        val stationIdTextView: TextView = itemView.findViewById(R.id.stationIdTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val reservationTimeTextView: TextView = itemView.findViewById(R.id.reservationTimeTextView)
    }
}