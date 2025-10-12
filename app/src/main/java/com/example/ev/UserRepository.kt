package com.example.ev

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A repository to manage user data from the network and local database.
 */
class UserRepository(context: Context) {

    private val userDao = UserDatabase.getDatabase(context).userDao()

    /**
     * Fetches user data from the local database.
     */
    suspend fun getLocalUser(userId: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUser(userId)
        }
    }

    /**
     * Fetches user data from the network and saves it to the local database.
     */
    suspend fun refreshUser(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = ApiClient.authenticatedApi.getUserDetails(userId).execute()
                if (response.isSuccessful) {
                    response.body()?.let { userProfile ->
                        val user = User(
                            id = userProfile.id,
                            username = userProfile.username,
                            fullName = userProfile.fullName,
                            nic = userProfile.nic,
                            email = userProfile.email,
                            role = SessionManager.getUserRole(), // Get role from session
                            isActive = userProfile.isActive,
                            createdAt = userProfile.createdAt
                        )
                        userDao.insertUser(user)
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
            }
        }
    }
}
