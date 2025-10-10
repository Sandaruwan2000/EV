package com.example.ev

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var roleTextView: TextView
    private lateinit var profileIcon: ImageView
    private lateinit var myBookingsButton: Button
    private lateinit var chargingStationRecyclerView: RecyclerView
    private lateinit var chargingStationAdapter: ChargingStationAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)
        dbHelper = DatabaseHelper(this)

        roleTextView = findViewById(R.id.roleTextView)
        profileIcon = findViewById(R.id.profileIcon)
        myBookingsButton = findViewById(R.id.myBookingsButton)
        chargingStationRecyclerView = findViewById(R.id.chargingStationRecyclerView)
        chargingStationRecyclerView.layoutManager = LinearLayoutManager(this)

        chargingStationAdapter = ChargingStationAdapter(emptyList()) { station ->
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("STATION_ID", station.id)
            startActivity(intent)
        }
        chargingStationRecyclerView.adapter = chargingStationAdapter

        val userEmail = sessionManager.getUserEmail()
        if (userEmail != null) {
            val user = dbHelper.getUser(userEmail)
            if (user != null) {
                roleTextView.text = "Role: ${user.role}"
                if (user.role == "EVOwner") {
                    myBookingsButton.visibility = View.VISIBLE
                    fetchChargingStations()
                }
            }
        }

        profileIcon.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        myBookingsButton.setOnClickListener {
            val intent = Intent(this, MyBookingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchChargingStations() {
        RetrofitInstance.getApi(sessionManager).getChargingStations().enqueue(object : Callback<List<ChargingStation>> {
            override fun onResponse(call: Call<List<ChargingStation>>, response: Response<List<ChargingStation>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val filteredStations = it.filter { station -> station.availableSlots > 0 && station.isActive }
                        chargingStationAdapter.updateStations(filteredStations)
                    }
                } else {
                    Log.e("MainActivity", "Failed to fetch charging stations: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<ChargingStation>>, t: Throwable) {
                Log.e("MainActivity", "Failed to fetch charging stations", t)
            }
        })
    }
}