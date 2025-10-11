package com.example.ev

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BookingsAdapter(
    private var bookings: List<BookingDetails>,
    private val onQrClicked: (BookingDetails) -> Unit,
    private val onEditClicked: (BookingDetails) -> Unit,
    private val onCancelClicked: (BookingDetails) -> Unit
) : RecyclerView.Adapter<BookingsAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_item, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.stationIdTextView.text = "Station ID: ${booking.chargingStationId}"
        holder.statusTextView.text = "Status: ${booking.status}"

        holder.itemView.setOnClickListener { onQrClicked(booking) }
        holder.editButton.setOnClickListener { onEditClicked(booking) }
        holder.cancelButton.setOnClickListener { onCancelClicked(booking) }
    }

    override fun getItemCount() = bookings.size

    fun updateBookings(newBookings: List<BookingDetails>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stationIdTextView: TextView = itemView.findViewById(R.id.stationIdTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val cancelButton: Button = itemView.findViewById(R.id.cancelButton)
    }
}