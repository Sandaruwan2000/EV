package com.example.ev

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var pendingBookingsCountTextView: TextView
    private lateinit var confirmedBookingsCountTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore the role-based check
        val userRole = SessionManager.getUserRole()
        if (userRole == "Backoffice") {
            startActivity(Intent(this, BookingConfirmationActivity::class.java))
            finish()
            return // Important to prevent the rest of the EVOwner UI from loading
        }

        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        pendingBookingsCountTextView = findViewById(R.id.pendingBookingsCountTextView)
        confirmedBookingsCountTextView = findViewById(R.id.confirmedBookingsCountTextView)

        findViewById<Button>(R.id.viewStationsButton).setOnClickListener {
            val intent = Intent(this, EvStationsActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.myBookingsButton).setOnClickListener {
            val intent = Intent(this, MyBookingsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // Only fetch bookings if this is not a Backoffice user
        if (SessionManager.getUserRole() != "Backoffice") {
            fetchUserBookings()
        }
    }

    private fun fetchUserBookings() {
        val userId = SessionManager.getUserId() ?: return

        // Use the authenticated API client to include the auth token
        ApiClient.authenticatedApi.getUserBookings(userId).enqueue(object : Callback<List<BookingDetails>> {
            override fun onResponse(call: Call<List<BookingDetails>>, response: Response<List<BookingDetails>>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val pendingCount = it.count { it.status.equals("Pending", ignoreCase = true) }
                        val confirmedCount = it.count { it.status.equals("Confirmed", ignoreCase = true) }
                        pendingBookingsCountTextView.text = pendingCount.toString()
                        confirmedBookingsCountTextView.text = confirmedCount.toString()
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Failed to fetch bookings", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<BookingDetails>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val intent = when (item.itemId) {
            R.id.nav_home -> Intent(this, MainActivity::class.java)
            R.id.nav_profile -> Intent(this, ProfileActivity::class.java)
            R.id.nav_my_bookings -> Intent(this, MyBookingsActivity::class.java)
            R.id.nav_logout -> {
                SessionManager.clearSession()
                Intent(this, Signin::class.java)
            }
            else -> null
        }

        if (intent != null) {
            startActivity(intent)
            if (item.itemId == R.id.nav_logout) {
                finish()
            }
        }

        drawerLayout.closeDrawers()
        return true
    }
}
