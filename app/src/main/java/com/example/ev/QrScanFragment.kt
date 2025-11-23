package com.example.ev

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QrScanFragment : Fragment() {

    private lateinit var bookingIdEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_qr_scan, container, false)

        bookingIdEditText = view.findViewById(R.id.bookingIdEditText)
        val scanButton: Button = view.findViewById(R.id.scanQrButton)
        val confirmButton: Button = view.findViewById(R.id.confirmBookingButton)

        // Retrieve the booking ID from the arguments and pre-fill the EditText
        val bookingIdFromArgs = arguments?.getString("bookingId")
        if (!bookingIdFromArgs.isNullOrEmpty()) {
            bookingIdEditText.setText(bookingIdFromArgs)
        }

        scanButton.setOnClickListener {
            // Use IntentIntegrator for Fragments
            IntentIntegrator.forSupportFragment(this).initiateScan()
        }

        confirmButton.setOnClickListener {
            val bookingId = bookingIdEditText.text.toString()
            if (bookingId.isNotEmpty()) {
                confirmBooking(bookingId)
            } else {
                Toast.makeText(requireContext(), "Please enter a booking ID or scan a QR code", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_LONG).show()
            } else {
                bookingIdEditText.setText(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun confirmBooking(bookingId: String) {
        val statusUpdate = BookingStatusUpdateRequest("Completed")
        // Use the authenticated API client to include the auth token
        ApiClient.authenticatedApi.updateBookingStatus(bookingId, statusUpdate).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Booking completed successfully", Toast.LENGTH_SHORT).show()
                    // Optionally clear the text field
                    bookingIdEditText.text.clear()
                } else {
                    Toast.makeText(requireContext(), "Failed to complete booking: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(requireContext(), "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
