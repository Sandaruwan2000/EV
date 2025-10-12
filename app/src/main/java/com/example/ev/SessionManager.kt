package com.example.ev

import android.content.Context
import android.content.SharedPreferences

/**
 * A singleton object to manage the user's session data using SharedPreferences.
 */
object SessionManager {
    private var prefs: SharedPreferences? = null

    private const val AUTH_TOKEN = "auth_token"
    private const val USER_ID = "user_id"
    private const val USER_ROLE = "user_role"

    /**
     * Initializes the SessionManager. Must be called once, in the Application class.
     */
    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        }
    }

    private fun requirePrefs(): SharedPreferences {
        return prefs ?: throw IllegalStateException("SessionManager must be initialized. Call initialize() in your Application class.")
    }

    fun saveSession(token: String, userId: String, role: String) {
        val editor = requirePrefs().edit()
        editor.putString(AUTH_TOKEN, token)
        editor.putString(USER_ID, userId)
        editor.putString(USER_ROLE, role)
        editor.apply()
    }

    fun getAuthToken(): String? {
        return requirePrefs().getString(AUTH_TOKEN, null)
    }

    fun getUserId(): String? {
        return requirePrefs().getString(USER_ID, null)
    }

    fun getUserRole(): String? {
        return requirePrefs().getString(USER_ROLE, null)
    }

    fun clearSession() {
        val editor = requirePrefs().edit()
        editor.clear()
        editor.apply()
    }
}
