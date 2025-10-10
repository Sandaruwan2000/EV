package com.example.ev

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var createNewBookingButton: Button
    private lateinit var bookingsRecyclerView: RecyclerView
    private lateinit var bookingsAdapter: BookingsAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        sessionManager = SessionManager(this)
        dbHelper = DatabaseHelper(this)

        createNewBookingButton = findViewById(R.id.createNewBookingButton)
        bookingsRecyclerView = findViewById(R.id.bookingsRecyclerView)
        bookingsRecyclerView.layoutManager = LinearLayoutManager(this)

        bookingsAdapter = BookingsAdapter(emptyList())
        bookingsRecyclerView.adapter = bookingsAdapter

        createNewBookingButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Enable the back button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fetchBookings()
    }

    private fun fetchBookings() {
        val userEmail = sessionManager.getUserEmail()
        val user = userEmail?.let { dbHelper.getUser(it) }
        if (user != null) {
            RetrofitInstance.getApi(sessionManager).getAllBookings().enqueue(object : Callback<List<BookingDetails>> {
                override fun onResponse(call: Call<List<BookingDetails>>, response: Response<List<BookingDetails>>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            val userBookings = it.filter { booking -> booking.ownerNIC == user.nic }
                            bookingsAdapter.updateBookings(userBookings)
                        }
                    } else {
                        Log.e("MyBookingsActivity", "Failed to fetch bookings: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<List<BookingDetails>>, t: Throwable) {
                    Log.e("MyBookingsActivity", "Failed to fetch bookings", t)
                }
            })
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}