package com.example.ev

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MyBookingsActivity : BaseActivity() {

    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var bookingsAdapter: BookingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        bookingsRecyclerView = findViewById(R.id.bookingsRecyclerView)
        bookingsRecyclerView.layoutManager = LinearLayoutManager(this)

        bookingsAdapter = BookingsAdapter(emptyList(),
            { booking -> showQrCodePopup(booking.id) },
            { booking -> handleEditBooking(booking) },
            { booking -> handleCancelBooking(booking) })
        bookingsRecyclerView.adapter = bookingsAdapter

        fetchBookings()
    }

    private fun fetchBookings() {
        ApiClient.authenticatedApi.getAllBookings().enqueue(object : Callback<List<BookingDetails>> {
            override fun onResponse(call: Call<List<BookingDetails>>, response: Response<List<BookingDetails>>) {
                if (response.isSuccessful) {
                    response.body()?.let { bookingsAdapter.updateBookings(it) }
                } else {
                    Toast.makeText(this@MyBookingsActivity, "Failed to fetch bookings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<BookingDetails>>, t: Throwable) {
                Toast.makeText(this@MyBookingsActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handleEditBooking(booking: BookingDetails) {
        if (isMoreThan12Hours(booking.reservationDateTime)) {
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("EDIT_BOOKING_ID", booking.id)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Cannot edit a booking less than 12 hours before reservation.", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleCancelBooking(booking: BookingDetails) {
        if (isMoreThan12Hours(booking.reservationDateTime)) {
            ApiClient.authenticatedApi.cancelBooking(booking.id).enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MyBookingsActivity, "Booking canceled successfully", Toast.LENGTH_SHORT).show()
                        fetchBookings() // Refresh the list
                    } else {
                        Toast.makeText(this@MyBookingsActivity, "Failed to cancel booking", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Toast.makeText(this@MyBookingsActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Cannot cancel a booking less than 12 hours before reservation.", Toast.LENGTH_LONG).show()
        }
    }

    private fun isMoreThan12Hours(reservationDateTime: String): Boolean {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val reservationDate = sdf.parse(reservationDateTime)
            val now = Date()
            val diff = reservationDate.time - now.time
            return diff > 12 * 60 * 60 * 1000
        } catch (e: Exception) {
            return false
        }
    }

    private fun showQrCodePopup(bookingId: String) {
        try {
            val bitMatrix = MultiFormatWriter().encode(bookingId, BarcodeFormat.QR_CODE, 400, 400)
            val bitmap: Bitmap = BarcodeEncoder().createBitmap(bitMatrix)
            val imageView = ImageView(this)
            imageView.setImageBitmap(bitmap)

            AlertDialog.Builder(this)
                .setTitle("Booking QR Code")
                .setView(imageView)
                .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
                .show()
        } catch (e: WriterException) {
            Toast.makeText(this, "Could not generate QR code", Toast.LENGTH_SHORT).show()
        }
    }
}