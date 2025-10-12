package com.example.ev

import android.app.Application

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize the global ApiClient, which also initializes the SessionManager.
        ApiClient.initialize(this)
    }
}
