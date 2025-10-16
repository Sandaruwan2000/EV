package com.example.ev

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : BaseActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var fullNameTextView: TextView
    private lateinit var nicTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var roleTextView: TextView
    private lateinit var editProfileButton: Button
    private lateinit var deactivateButton: Button
    private lateinit var stationSelectorCard: CardView
    private lateinit var stationSpinner: Spinner
    private lateinit var editSaveStationButton: Button

    private var stations: List<ChargingStation> = emptyList()
    private var isEditingStation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        bindViews()

        val userId = SessionManager.getUserId()
        val userRole = SessionManager.getUserRole()
        if (userId == null || userRole == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            logout()
            return
        }

        // Set the role from SessionManager
        roleTextView.text = "Role: $userRole"

        when (userRole) {
            "EVOwner" -> loadEvOwnerProfile(userId)
            "Backoffice" -> loadBackofficeProfile(userId)
            else -> {
                Toast.makeText(this, "Profile not available for this role", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        findViewById<Button>(R.id.logoutButton).setOnClickListener { logout() }
    }

    private fun bindViews() {
        usernameTextView = findViewById(R.id.usernameTextView)
        fullNameTextView = findViewById(R.id.fullNameTextView)
        nicTextView = findViewById(R.id.nicTextView)
        emailTextView = findViewById(R.id.emailTextView)
        roleTextView = findViewById(R.id.roleTextView)
        editProfileButton = findViewById(R.id.editProfileButton)
        deactivateButton = findViewById(R.id.deactivateButton)
        stationSelectorCard = findViewById(R.id.stationSelectorCard)
        stationSpinner = findViewById(R.id.stationSpinner)
        editSaveStationButton = findViewById(R.id.editSaveStationButton)
    }

    private fun loadEvOwnerProfile(userId: String) {
        stationSelectorCard.visibility = View.GONE
        ApiClient.authenticatedApi.getEvOwnerUserDetails(userId).enqueue(object : Callback<UserProfileResponse> {
            override fun onResponse(call: Call<UserProfileResponse>, response: Response<UserProfileResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        usernameTextView.text = "Username: ${it.username}"
                        fullNameTextView.text = "Full Name: ${it.fullName}"
                        nicTextView.text = "NIC: ${it.nic}"
                        emailTextView.text = "Email: ${it.email}"
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to load EV Owner profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserProfileResponse>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

        editProfileButton.setOnClickListener { startActivity(Intent(this, EditProfileActivity::class.java)) }
        deactivateButton.setOnClickListener { showDeactivationConfirmDialog() }
    }

    private fun loadBackofficeProfile(userId: String) {
        editProfileButton.visibility = View.GONE
        deactivateButton.visibility = View.GONE
        stationSelectorCard.visibility = View.VISIBLE

        setupStationSpinner()

        ApiClient.authenticatedApi.getUserDetails(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        usernameTextView.text = "Username: ${it.username}"
                        fullNameTextView.text = "Full Name: ${it.fullName ?: "N/A"}"
                        nicTextView.text = "NIC: ${it.nic ?: "N/A"}"
                        emailTextView.text = "Email: ${it.email ?: "N/A"}"
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to load Backoffice profile", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<User>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupStationSpinner() {
        ApiClient.authenticatedApi.getChargingStations().enqueue(object : Callback<List<ChargingStation>> {
            override fun onResponse(call: Call<List<ChargingStation>>, response: Response<List<ChargingStation>>) {
                if (response.isSuccessful) {
                    stations = response.body() ?: emptyList()
                    val stationNames = stations.map { it.name }
                    val adapter = ArrayAdapter(this@ProfileActivity, android.R.layout.simple_spinner_item, stationNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    stationSpinner.adapter = adapter

                    val selectedStationId = SessionManager.getSelectedStationId()
                    if (selectedStationId != null) {
                        val position = stations.indexOfFirst { it.id == selectedStationId }
                        if (position >= 0) {
                            stationSpinner.setSelection(position)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<List<ChargingStation>>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "Failed to load stations", Toast.LENGTH_SHORT).show()
            }
        })

        editSaveStationButton.setOnClickListener {
            if (isEditingStation) {
                val selectedStation = stations[stationSpinner.selectedItemPosition]
                SessionManager.saveStationSelection(selectedStation.id, selectedStation.name)
                stationSpinner.isEnabled = false
                editSaveStationButton.text = "Edit"
                isEditingStation = false
                Toast.makeText(this, "Station saved: ${selectedStation.name}", Toast.LENGTH_SHORT).show()
            } else {
                stationSpinner.isEnabled = true
                editSaveStationButton.text = "Save"
                isEditingStation = true
            }
        }
    }

    private fun showDeactivationConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Deactivate Account")
            .setMessage("Are you sure you want to deactivate your account? This action cannot be undone.")
            .setPositiveButton("Deactivate") { _, _ -> deactivateAccount() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deactivateAccount() {
        val userId = SessionManager.getUserId() ?: return
        ApiClient.authenticatedApi.deactivateUser(userId).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Account deactivated successfully.", Toast.LENGTH_SHORT).show()
                    logout()
                } else {
                    Toast.makeText(this@ProfileActivity, "Failed to deactivate account: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(this@ProfileActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun logout() {
        SessionManager.clearSession()
        Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, Signin::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
