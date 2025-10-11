package com.example.ev

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * An abstract base activity that enforces session validation.
 * Activities that require a user to be logged in should extend this class.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if a user session is active.
        if (SessionManager.getAuthToken() == null) {
            // If not, redirect to the Signin screen.
            val intent = Intent(this, Signin::class.java)
            // Clear the activity stack to prevent the user from navigating back.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            // Finish the current activity.
            finish()
            // Stop further execution of the onCreate method in the child activity.
            return
        }
    }
}
