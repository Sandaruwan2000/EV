package com.example.ev

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2 // Incremented database version
        private const val DATABASE_NAME = "UserManager.db"
        private const val TABLE_USER = "user"
        private const val KEY_ID = "id"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_PASSWORD = "password"
        private const val KEY_NIC = "nic"
        private const val KEY_ROLE = "role"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_USER_TABLE = ("CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_FIRST_NAME + " TEXT,"
                + KEY_LAST_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_PHONE + " TEXT,"
                + KEY_PASSWORD + " TEXT,"
                + KEY_NIC + " TEXT,"
                + KEY_ROLE + " TEXT" + ")")
        db?.execSQL(CREATE_USER_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    fun addUser(user: User): Long {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(KEY_FIRST_NAME, user.firstName)
        contentValues.put(KEY_LAST_NAME, user.lastName)
        contentValues.put(KEY_EMAIL, user.email)
        contentValues.put(KEY_PHONE, user.phone)
        contentValues.put(KEY_PASSWORD, user.password)
        contentValues.put(KEY_NIC, user.nic)
        contentValues.put(KEY_ROLE, user.role)
        val success = db.insert(TABLE_USER, null, contentValues)
        db.close()
        return success
    }

    fun getUser(email: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USER, arrayOf(KEY_ID, KEY_FIRST_NAME, KEY_LAST_NAME, KEY_EMAIL, KEY_PHONE, KEY_PASSWORD, KEY_NIC, KEY_ROLE),
            "$KEY_EMAIL = ?", arrayOf(email), null, null, null, null)

        if (cursor != null && cursor.moveToFirst()) {
            val user = User(
                cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_FIRST_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHONE)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_PASSWORD)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_NIC)),
                cursor.getString(cursor.getColumnIndexOrThrow(KEY_ROLE))
            )
            cursor.close()
            db.close()
            return user
        }
        cursor?.close()
        db.close()
        return null
    }
}