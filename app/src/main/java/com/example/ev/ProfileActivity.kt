package com.example.ev

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class ProfileActivity : AppCompatActivity() {

    private lateinit var usernameTextView: TextView
    private lateinit var fullNameTextView: TextView
    private lateinit var nicTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var roleTextView: TextView
    private lateinit var logoutButton: Button
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        dbHelper = DatabaseHelper(this)
        sessionManager = SessionManager(this)

        usernameTextView = findViewById(R.id.usernameTextView)
        fullNameTextView = findViewById(R.id.fullNameTextView)
        nicTextView = findViewById(R.id.nicTextView)
        emailTextView = findViewById(R.id.emailTextView)
        phoneTextView = findViewById(R.id.phoneTextView)
        roleTextView = findViewById(R.id.roleTextView)
        logoutButton = findViewById(R.id.logoutButton)

        val email = sessionManager.getUserEmail()
        if (email != null) {
            val user = dbHelper.getUser(email)
            if (user != null) {
                usernameTextView.text = "Username: ${user.firstName}"
                fullNameTextView.text = "Full Name: ${user.firstName} ${user.lastName}"
                nicTextView.text = "NIC: ${user.nic}"
                emailTextView.text = "Email: ${user.email}"
                phoneTextView.text = "Phone: ${user.phone}"
                roleTextView.text = "Role: ${user.role}"
            }
        }

        logoutButton.setOnClickListener {
            sessionManager.clear()
            val intent = Intent(this, Signin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}