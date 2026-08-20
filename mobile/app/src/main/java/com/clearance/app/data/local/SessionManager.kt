package com.clearance.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.clearance.app.data.api.dto.UserDto
import com.google.gson.Gson

/**
 * Local token/user storage.
 *
 * This is the Android equivalent of client/src/services/storage.js
 * from the web app: saveToken/getToken/saveUser/getUser/clearAll.
 *
 * The web app uses sessionStorage (cleared when the browser tab
 * closes). Android has no direct equivalent of "tab closed", so this
 * uses SharedPreferences instead — good enough for this school
 * project, matching the "secure enough for a school project" scope
 * from the original instructions. No new Gradle dependency was
 * required for this.
 *
 * MUST be initialized once via SessionManager.init(context) before
 * first use — this is done in MainActivity.onCreate().
 */
object SessionManager {

    private const val PREFS_NAME = "clearance_prefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER = "user"

    private var prefs: SharedPreferences? = null
    private val gson = Gson()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
        }
    }

    fun saveToken(token: String) {
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun getToken(): String? = prefs?.getString(KEY_TOKEN, null)

    fun clearToken() {
        prefs?.edit()?.remove(KEY_TOKEN)?.apply()
    }

    fun saveUser(user: UserDto) {
        prefs?.edit()?.putString(KEY_USER, gson.toJson(user))?.apply()
    }

    fun getUser(): UserDto? {
        val json = prefs?.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(json, UserDto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearUser() {
        prefs?.edit()?.remove(KEY_USER)?.apply()
    }

    /** Clears everything — Android equivalent of storage.js's clearAll(). */
    fun clearAll() {
        prefs?.edit()?.clear()?.apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null
}
