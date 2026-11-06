package com.example.service.auth

import android.content.Context
import android.webkit.CookieManager
import java.io.File

object PismoswieteAuthManager {
    private const val PREFS_NAME = "pismoswiete_auth_prefs"
    private const val KEY_COOKIES = "pismoswiete_cookies"
    private const val KEY_USER_EMAIL = "pismoswiete_user_email"

    fun getCookies(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_COOKIES, null)
        if (!saved.isNullOrBlank()) return saved

        return try {
            val cmCookies = CookieManager.getInstance().getCookie("https://pismoswiete.pl")
            if (!cmCookies.isNullOrBlank() && cmCookies.contains("PHPSESSID")) {
                cmCookies
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun saveCookies(context: Context, cookies: String, email: String? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_COOKIES, cookies)
            .apply {
                if (email != null) putString(KEY_USER_EMAIL, email)
            }
            .apply()

        try {
            val cm = CookieManager.getInstance()
            cm.setAcceptCookie(true)
            cookies.split(";").forEach { cookiePart ->
                cm.setCookie("https://pismoswiete.pl", cookiePart.trim())
            }
            cm.flush()
        } catch (ignored: Exception) {}

        clearBibleCache(context)
    }

    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        try {
            val cm = CookieManager.getInstance()
            cm.removeAllCookies(null)
            cm.flush()
        } catch (ignored: Exception) {}
        clearBibleCache(context)
    }

    fun isLoggedIn(context: Context): Boolean {
        val c = getCookies(context)
        return !c.isNullOrBlank() && (c.contains("PHPSESSID") || c.contains("REMEMBERME"))
    }

    fun getUserEmail(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun clearBibleCache(context: Context) {
        try {
            val dir = File(context.cacheDir, "bible_cache_v4")
            if (dir.exists()) {
                dir.deleteRecursively()
            }
            File(context.cacheDir, "bible_cache_v3").deleteRecursively()
            File(context.cacheDir, "bible_cache_v2").deleteRecursively()
        } catch (ignored: Exception) {}
    }
}
