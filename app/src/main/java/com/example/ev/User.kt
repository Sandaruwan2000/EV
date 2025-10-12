package com.example.ev

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val username: String?,
    val fullName: String?,
    val nic: String?,
    val email: String?,
    val role: String?,
    val isActive: Boolean?,
    val createdAt: String?
)
