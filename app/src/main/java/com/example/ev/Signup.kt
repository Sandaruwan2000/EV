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

class Signup : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var fullNameEditText: EditText
    private lateinit var nicEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var createAccountButton: Button
    private lateinit var loginTextView: TextView
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        usernameEditText = findViewById(R.id.usernameEditText)
        fullNameEditText = findViewById(R.id.fullNameEditText)
        nicEditText = findViewById(R.id.nicEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        roleSpinner = findViewById(R.id.roleSpinner)
        createAccountButton = findViewById(R.id.signUpButton) // Keep the same ID for now
        loginTextView = findViewById(R.id.signInTextView) // Keep the same ID for now
        dbHelper = DatabaseHelper(this)

        val roles = arrayOf("Backoffice", "StationOperator", "EVOwner")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = adapter

        createAccountButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val fullName = fullNameEditText.text.toString()
            val nic = nicEditText.text.toString()
            val email = emailEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()
            val selectedRole = roleSpinner.selectedItem.toString()

            if (username.isNotEmpty() && fullName.isNotEmpty() && nic.isNotEmpty() && email.isNotEmpty() && phone.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    val user = User(firstName = fullName, lastName = "", email = email, phone = phone, password = password, nic = nic, role = selectedRole)
                    val result = dbHelper.addUser(user)
                    if (result > -1) {
                        Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, Signin::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Signup failed. Email might already exist.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        loginTextView.setOnClickListener {
            val intent = Intent(this, Signin::class.java)
            startActivity(intent)
        }
    }
}