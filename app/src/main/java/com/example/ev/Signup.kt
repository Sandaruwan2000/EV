package com.example.ev

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Signup : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var fullNameEditText: EditText
    private lateinit var nicEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var signUpButton: Button
    private lateinit var signInRedirectTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        usernameEditText = findViewById(R.id.usernameEditText)
        fullNameEditText = findViewById(R.id.fullNameEditText)
        nicEditText = findViewById(R.id.nicEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        roleSpinner = findViewById(R.id.roleSpinner)
        signUpButton = findViewById(R.id.signUpButton)
        signInRedirectTextView = findViewById(R.id.signInTextView)

        val roles = arrayOf("EVOwner", "Backoffice", "StationOperator")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        signUpButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val fullName = fullNameEditText.text.toString()
            val nic = nicEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val selectedRole = roleSpinner.selectedItem.toString()

            if (username.isNotEmpty() && fullName.isNotEmpty() && nic.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                val userRegistration = UserRegistration(
                    username = username,
                    passwordHash = password, // The server should handle hashing
                    role = selectedRole,
                    isActive = true,
                    nic = nic,
                    fullName = fullName,
                    email = email
                )

                ApiClient.api.registerUser(userRegistration).enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@Signup, "Registration successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@Signup, Signin::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@Signup, "Registration failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Toast.makeText(this@Signup, "Registration failed: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        signInRedirectTextView.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
        }
    }
}