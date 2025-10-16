package com.example.ev

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private var prefs: SharedPreferences? = null

    private const val AUTH_TOKEN = "auth_token"
    private const val USER_ID = "user_id"
    private const val USER_ROLE = "user_role"
    private const val SELECTED_STATION_ID = "selected_station_id"
    private const val SELECTED_STATION_NAME = "selected_station_name"

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        }
    }

    private fun requirePrefs(): SharedPreferences {
        return prefs ?: throw IllegalStateException("SessionManager must be initialized.")
    }

    fun saveSession(token: String, userId: String, role: String) {
        val editor = requirePrefs().edit()
        editor.putString(AUTH_TOKEN, token)
        editor.putString(USER_ID, userId)
        editor.putString(USER_ROLE, role)
        editor.apply()
    }

    fun saveStationSelection(stationId: String, stationName: String) {
        val editor = requirePrefs().edit()
        editor.putString(SELECTED_STATION_ID, stationId)
        editor.putString(SELECTED_STATION_NAME, stationName)
        editor.apply()
    }

    fun getAuthToken(): String? = requirePrefs().getString(AUTH_TOKEN, null)

    fun getUserId(): String? = requirePrefs().getString(USER_ID, null)

    fun getUserRole(): String? = requirePrefs().getString(USER_ROLE, null)

    fun getSelectedStationId(): String? = requirePrefs().getString(SELECTED_STATION_ID, null)

    fun getSelectedStationName(): String? = requirePrefs().getString(SELECTED_STATION_NAME, null)

    /**
     * Clears user-specific session data but preserves the station selection.
     */
    fun clearSession() {
        val editor = requirePrefs().edit()
        editor.remove(AUTH_TOKEN)
        editor.remove(USER_ID)
        editor.remove(USER_ROLE)
        editor.apply()
    }
}
