package com.example.ev

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // If you are using the Android Emulator, 10.0.2.2 is the address to connect to your computer.
    // If you are using a physical device, you MUST replace 10.0.2.2 with your computer's IP address.
    private const val BASE_URL = "http://10.0.2.2:5000/"

    fun getClient(sessionManager: SessionManager): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor {
                val request = it.request().newBuilder()
                val token = sessionManager.getAuthToken()
                if (token != null) {
                    request.addHeader("Authorization", token)
                }
                it.proceed(request.build())
            }
            .build()
    }

    fun getApi(sessionManager: SessionManager): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getClient(sessionManager))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
