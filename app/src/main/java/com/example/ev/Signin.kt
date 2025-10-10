package com.example.ev

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class Signin : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var createAccountTextView: TextView
    private lateinit var sessionManager: SessionManager
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        sessionManager = SessionManager(this)
        dbHelper = DatabaseHelper(this)

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.signInButton)
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView)
        createAccountTextView = findViewById(R.id.signUpTextView)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val user = dbHelper.getUser(email)
                if (user != null && user.password == password) {
                    // Save user email to session
                    sessionManager.saveUserEmail(email)
                    // For now, we are not using the API token for login
                    // If your booking API still needs a token, we must generate one or change the API.
                    // For now, let's assume booking will work without a token.

                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        createAccountTextView.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }

        forgotPasswordTextView.setOnClickListener {
            // TODO: Navigate to Forgot Password screen
            Toast.makeText(this, "Navigate to Forgot Password", Toast.LENGTH_SHORT).show()
        }
    }
}