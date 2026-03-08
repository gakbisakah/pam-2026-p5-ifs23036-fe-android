package org.delcom.pam_p5_ifs23036.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class AuthTokenPref(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("auth_token_prefs", Context.MODE_PRIVATE)

    // Key untuk menyimpan token
    private val AUTH_TOKEN_KEY = "AUTH_TOKEN_KEY"
    private val REFRESH_TOKEN_KEY = "REFRESH_TOKEN_KEY"
    private val USER_ID_KEY = "USER_ID_KEY"

    // Menyimpan token
    fun saveAuthToken(token: String?) {
        sharedPreferences.edit { putString(AUTH_TOKEN_KEY, token) }
    }

    // Mengambil token
    fun getAuthToken(): String? {
        return sharedPreferences.getString(AUTH_TOKEN_KEY, null)
    }

    // Menghapus token
    fun clearAuthToken() {
        sharedPreferences.edit { remove(AUTH_TOKEN_KEY) }
    }

    // Menyimpan refresh token
    fun saveRefreshToken(token: String?) {
        sharedPreferences.edit { putString(REFRESH_TOKEN_KEY, token) }
    }

    // Mengambil refresh token
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(REFRESH_TOKEN_KEY, null)
    }

    // Menghapus refresh token
    fun clearRefreshToken() {
        sharedPreferences.edit { remove(REFRESH_TOKEN_KEY) }
    }

    // Menyimpan User ID
    fun saveUserId(userId: String?) {
        sharedPreferences.edit { putString(USER_ID_KEY, userId) }
    }

    // Mengambil User ID
    fun getUserId(): String? {
        return sharedPreferences.getString(USER_ID_KEY, null)
    }

    // Menghapus User ID
    fun clearUserId() {
        sharedPreferences.edit { remove(USER_ID_KEY) }
    }
}