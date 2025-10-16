package com.example.ev

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class Signin : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var createAccountTextView: TextView
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Auto-login if session exists
        if (SessionManager.getAuthToken() != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_signin)

        userRepository = UserRepository(this)
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.signInButton)
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView)
        createAccountTextView = findViewById(R.id.signUpTextView)

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val authRequest = AuthRequest(username, password)
                ApiClient.api.authenticate(authRequest).enqueue(object : Callback<LoginApiResponse> {
                    override fun onResponse(call: Call<LoginApiResponse>, response: Response<LoginApiResponse>) {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()

                            if (loginResponse?.token != null && loginResponse.userId != null && loginResponse.role != null) {
                                SessionManager.saveSession(loginResponse.token, loginResponse.userId, loginResponse.role)
                                lifecycleScope.launch {
                                    userRepository.refreshUser(loginResponse.userId)
                                }
                                Toast.makeText(this@Signin, "Login successful", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@Signin, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@Signin, "Invalid response from server", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@Signin, "Invalid username or password", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginApiResponse>, t: Throwable) {
                        if (t is IOException) {
                            Toast.makeText(this@Signin, "Could not connect to server. Please check your network connection.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this@Signin, "Login failed: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                })
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        createAccountTextView.setOnClickListener {
            val intent = Intent(this, Signup::class.java)
            startActivity(intent)
        }

        forgotPasswordTextView.setOnClickListener {
            Toast.makeText(this, "Navigate to Forgot Password", Toast.LENGTH_SHORT).show()
        }
    }
}