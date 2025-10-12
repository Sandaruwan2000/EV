package com.example.ev

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileActivity : BaseActivity() {

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userRepository = UserRepository(this)

        val usernameTextView: TextView = findViewById(R.id.usernameTextView)
        val fullNameTextView: TextView = findViewById(R.id.fullNameTextView)
        val nicTextView: TextView = findViewById(R.id.nicTextView)
        val emailTextView: TextView = findViewById(R.id.emailTextView)
        val roleTextView: TextView = findViewById(R.id.roleTextView)
        val editProfileButton: Button = findViewById(R.id.editProfileButton)
        val deactivateButton: Button = findViewById(R.id.deactivateButton)
        val logoutButton: Button = findViewById(R.id.logoutButton)

        val userId = SessionManager.getUserId()
        val userRole = SessionManager.getUserRole()

        if (userRole == "EVOwner") {
            if (userId != null) {
                lifecycleScope.launch {
                    userRepository.refreshUser(userId) // Refresh data first
                    val user = userRepository.getLocalUser(userId)
                    user?.let {
                        usernameTextView.text = "Username: ${it.username}"
                        fullNameTextView.text = "Full Name: ${it.fullName}"
                        nicTextView.text = "NIC: ${it.nic}"
                        emailTextView.text = "Email: ${it.email}"
                        roleTextView.text = "Role: ${it.role}"
                    }
                }
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Profile not available for this role", Toast.LENGTH_SHORT).show()
            finish()
        }

        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        deactivateButton.setOnClickListener {
            showDeactivationConfirmDialog()
        }

        logoutButton.setOnClickListener {
            logout()
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
        val userId = SessionManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

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