package com.example.ev

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class BookingsAdapter(
    private var bookings: List<BookingDetails>,
    private val showActions: Boolean = false,
    private val onQrClicked: ((BookingDetails) -> Unit)? = null,
    private val onEditClicked: ((BookingDetails) -> Unit)? = null,
    private val onCancelClicked: ((BookingDetails) -> Unit)? = null
) : RecyclerView.Adapter<BookingsAdapter.BookingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_item, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val booking = bookings[position]
        holder.statusTextView.text = "Status: ${booking.status}"
        holder.startTimeTextView.text = "Start: ${formatDate(booking.startTime)}"
        holder.endTimeTextView.text = "End: ${formatDate(booking.endTime)}"

        Glide.with(holder.itemView.context)
            .load(R.drawable.lk) // Load the image
            .into(holder.bookingImageView)

        if (showActions && booking.status.equals("Pending", ignoreCase = true)) {
            holder.actionsLayout.visibility = View.VISIBLE
            holder.editButton.setOnClickListener { onEditClicked?.invoke(booking) }
            holder.cancelButton.setOnClickListener { onCancelClicked?.invoke(booking) }
        } else {
            holder.actionsLayout.visibility = View.GONE
        }

        // QR code click can be on the whole item
        holder.itemView.setOnClickListener {
            if (showActions) {
                onQrClicked?.invoke(booking)
            }
        }
    }

    override fun getItemCount() = bookings.size

    fun updateBookings(newBookings: List<BookingDetails>) {
        bookings = newBookings
        notifyDataSetChanged()
    }

    private fun formatDate(dateString: String?): String {
        if (dateString == null) return "N/A"

        val outputFormat = SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault())
        val inputFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        )

        for (format in inputFormats) {
            try {
                format.timeZone = TimeZone.getTimeZone("UTC")
                val date = format.parse(dateString)
                if (date != null) {
                    return outputFormat.format(date)
                }
            } catch (e: ParseException) {
                // Continue to next format
            }
        }

        return "Invalid Date"
    }

    class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookingImageView: ImageView = itemView.findViewById(R.id.bookingImageView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val startTimeTextView: TextView = itemView.findViewById(R.id.startTimeTextView)
        val endTimeTextView: TextView = itemView.findViewById(R.id.endTimeTextView)
        val actionsLayout: LinearLayout = itemView.findViewById(R.id.actionsLayout)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val cancelButton: Button = itemView.findViewById(R.id.cancelButton)
    }
}