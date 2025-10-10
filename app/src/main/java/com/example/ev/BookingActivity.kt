package com.example.ev

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingActivity : AppCompatActivity() {

    private lateinit var stationIdTextView: TextView
    private lateinit var bookNowButton: Button
    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        sessionManager = SessionManager(this)
        dbHelper = DatabaseHelper(this)

        stationIdTextView = findViewById(R.id.stationIdTextView)
        bookNowButton = findViewById(R.id.bookNowButton)

        val stationId = intent.getStringExtra("STATION_ID")
        stationIdTextView.text = "Station ID: $stationId"

        bookNowButton.setOnClickListener {
            if (stationId != null) {
                val userEmail = sessionManager.getUserEmail()
                val user = userEmail?.let { dbHelper.getUser(it) }
                if (user != null) {
                    val booking = Booking(
                        chargingStationId = stationId,
                        evOwnerUserId = user.id.toString(),
                        ownerNIC = user.nic,
                        slotNumber = 0, // Hardcoded for now
                        reservationDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()),
                        createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date()),
                        status = "Active"
                    )

                    RetrofitInstance.getApi(sessionManager).bookStation(booking).enqueue(object : Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@BookingActivity, "Booking successful", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@BookingActivity, "Booking failed", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Toast.makeText(this@BookingActivity, "Booking failed: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
    }
}