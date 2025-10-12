package com.example.ev

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProfileActivity : BaseActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var nicEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var saveChangesButton: Button
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        userRepository = UserRepository(this)
        fullNameEditText = findViewById(R.id.fullNameEditText)
        nicEditText = findViewById(R.id.nicEditText)
        emailEditText = findViewById(R.id.emailEditText)
        saveChangesButton = findViewById(R.id.saveChangesButton)

        loadUserProfile()

        saveChangesButton.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadUserProfile() {
        val userId = SessionManager.getUserId()
        if (userId != null) {
            lifecycleScope.launch {
                val user = userRepository.getLocalUser(userId)
                if (user != null) {
                    fullNameEditText.setText(user.fullName)
                    nicEditText.setText(user.nic)
                    emailEditText.setText(user.email)
                } else {
                    Toast.makeText(this@EditProfileActivity, "User data not found.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveChanges() {
        val fullName = fullNameEditText.text.toString()
        val nic = nicEditText.text.toString()
        val email = emailEditText.text.toString()

        if (fullName.isEmpty() || nic.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = SessionManager.getUserId()
        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        val updateRequest = UpdateProfileRequest(fullName, nic, email)
        ApiClient.authenticatedApi.updateUserProfile(userId, updateRequest).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@EditProfileActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    lifecycleScope.launch { userRepository.refreshUser(userId) } // Refresh local data
                    finish()
                } else {
                    Toast.makeText(this@EditProfileActivity, "Failed to update profile: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Toast.makeText(this@EditProfileActivity, "An error occurred: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}