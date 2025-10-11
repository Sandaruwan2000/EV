package com.example.ev

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A singleton object to manage all API clients, both authenticated and unauthenticated.
 */
object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:5000/"

    // Unauthenticated API client for login, registration, etc.
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Authenticated API client that includes the authorization token.
    lateinit var authenticatedApi: ApiService
        private set

    /**
     * Initializes the authenticated API client. This must be called once, typically in the Application class.
     */
    fun initialize(context: Context) {
        // Initialize the SessionManager, which is also a singleton.
        SessionManager.initialize(context.applicationContext)

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                // Intercept the request to add the Authorization header.
                val token = SessionManager.getAuthToken()
                val request = chain.request().newBuilder()
                if (token != null) {
                    request.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(request.build())
            }
            .build()

        authenticatedApi = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
