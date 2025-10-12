package com.example.ev

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : BaseActivity() {

    private lateinit var stationRecyclerView: RecyclerView
    private lateinit var stationAdapter: ChargingStationAdapter
    private lateinit var placeholderTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        stationRecyclerView = findViewById(R.id.stationRecyclerView)
        placeholderTextView = findViewById(R.id.placeholderTextView)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val userRole = SessionManager.getUserRole()
        if (userRole == "EVOwner") {
            setupEVOwnerUI(bottomNavigationView)
        } else {
            setupNonEVOwnerUI(userRole, bottomNavigationView)
        }
    }

    private fun setupEVOwnerUI(navView: BottomNavigationView) {
        stationRecyclerView.visibility = View.VISIBLE
        placeholderTextView.visibility = View.GONE

        stationRecyclerView.layoutManager = LinearLayoutManager(this)
        stationAdapter = ChargingStationAdapter(emptyList()) { station ->
            val intent = Intent(this, BookingActivity::class.java)
            intent.putExtra("STATION_ID", station.id) // Revert to passing only the ID
            startActivity(intent)
        }
        stationRecyclerView.adapter = stationAdapter
        fetchChargingStations()

        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    fetchChargingStations()
                    true
                }
                R.id.navigation_my_bookings -> {
                    val intent = Intent(this, MyBookingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupNonEVOwnerUI(role: String?, navView: BottomNavigationView) {
        stationRecyclerView.visibility = View.GONE
        placeholderTextView.visibility = View.VISIBLE
        placeholderTextView.text = "Welcome, $role! Your dashboard is under construction."

        navView.menu.findItem(R.id.navigation_my_bookings).isVisible = false

        navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchChargingStations() {
        ApiClient.authenticatedApi.getChargingStations().enqueue(object : Callback<List<ChargingStation>> {
            override fun onResponse(call: Call<List<ChargingStation>>, response: Response<List<ChargingStation>>) {
                if (response.isSuccessful) {
                    val activeStations = response.body()?.filter { it.isActive } ?: emptyList()
                    stationAdapter.updateStations(activeStations)
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch stations", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ChargingStation>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                SessionManager.clearSession()
                Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Signin::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}