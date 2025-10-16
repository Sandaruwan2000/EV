package com.example.ev

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class MyBookingsActivity : BaseActivity() {

    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var bookingsAdapter: BookingsAdapter
    private lateinit var statusFilterSpinner: Spinner

    private var allMyBookings: List<BookingDetails> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        bookingsRecyclerView = findViewById(R.id.bookingsRecyclerView)
        statusFilterSpinner = findViewById(R.id.statusFilterSpinner)
        bookingsRecyclerView.layoutManager = LinearLayoutManager(this)

        bookingsAdapter = BookingsAdapter(
            bookings = emptyList(),
            showActions = true,
            onQrClicked = { booking -> showQrCodePopup(booking.id) },
            onEditClicked = { booking -> handleEditBooking(booking) },
            onCancelClicked = { booking -> handleCancelBooking(booking) }
        )
        bookingsRecyclerView.adapter = bookingsAdapter

        setupFilterSpinner()
    }

    override fun onResume() {
        super.onResume()
        fetchUserBookings() // Fetch bookings every time the activity is shown
    }

    private fun setupFilterSpinner() {
        val filterOptions = arrayOf("All", "Pending", "Confirmed", "Completed", "Cancelled")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        statusFilterSpinner.adapter = spinnerAdapter

        statusFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterBookings(filterOptions[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) { /* Do nothing */ }
        }
    }

    private fun fetchUserBookings() {
        val userId = SessionManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.authenticatedApi.getUserBookings(userId).enqueue(object : Callback<List<BookingDetails>> {
            override fun onResponse(call: Call<List<BookingDetails>>, response: Response<List<BookingDetails>>) {
                if (response.isSuccessful) {
                    allMyBookings = response.body() ?: emptyList()
                    filterBookings(statusFilterSpinner.selectedItem.toString())
                } else {
                    Toast.makeText(this@MyBookingsActivity, "Failed to fetch bookings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<BookingDetails>>, t: Throwable) {
                Toast.makeText(this@MyBookingsActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filterBookings(status: String) {
        val filteredList = if (status.equals("All", ignoreCase = true)) {
            allMyBookings
        } else {
            allMyBookings.filter { it.status.equals(status, ignoreCase = true) }
        }
        bookingsAdapter.updateBookings(filteredList)
    }

    private fun handleEditBooking(booking: BookingDetails) {
        if (isWithinGracePeriod(booking.createdAt)) {
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("EDIT_BOOKING_ID", booking.id)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Bookings can only be modified within 12 hours of creation.", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleCancelBooking(booking: BookingDetails) {
        if (isWithinGracePeriod(booking.createdAt)) {
            ApiClient.authenticatedApi.cancelBooking(booking.id).enqueue(object : Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MyBookingsActivity, "Booking canceled successfully", Toast.LENGTH_SHORT).show()
                        fetchUserBookings() // Refresh the list
                    } else {
                        Toast.makeText(this@MyBookingsActivity, "Failed to cancel booking: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Toast.makeText(this@MyBookingsActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Bookings can only be modified within 12 hours of creation.", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Checks if a booking is still within its 12-hour grace period for modifications.
     * @return true if the booking can be modified, false otherwise.
     */
    private fun isWithinGracePeriod(creationDateTime: String?): Boolean {
        if (creationDateTime == null) return false // Cannot modify if no date is available

        val inputFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        )

        var creationDate: Date? = null
        for (format in inputFormats) {
            try {
                format.timeZone = TimeZone.getTimeZone("UTC")
                creationDate = format.parse(creationDateTime)
                if (creationDate != null) break
            } catch (e: ParseException) {
                // Continue to the next format
            }
        }

        if (creationDate == null) return false // Cannot modify if date is unparsable

        val now = Date()
        val diff = now.time - creationDate.time
        // Return true if LESS than 12 hours have passed
        return diff < 12 * 60 * 60 * 1000
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