package com.example.service.auth

import android.content.Context
import android.webkit.CookieManager
import java.io.File

object PismoswieteAuthManager {
    private const val PREFS_NAME = "pismoswiete_auth_prefs"
    private const val KEY_COOKIES = "pismoswiete_cookies"
    private const val KEY_USER_EMAIL = "pismoswiete_user_email"
    private const val KEY_IS_LOGGED_IN = "pismoswiete_is_logged_in"

    /**
     * Zwraca ciasteczka sesji TYLKO jeśli użytkownik faktycznie przeszedł pomyślne logowanie.
     * Nigdy nie zwraca ciasteczek gościa (anonimowego PHPSESSID).
     */
    fun getCookies(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return null

        val saved = prefs.getString(KEY_COOKIES, null)
        if (!saved.isNullOrBlank()) return saved

        return null
    }

    /**
     * Zapisuje zweryfikowaną sesję logowania.
     */
    fun saveCookies(context: Context, cookies: String, email: String? = null) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
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

    /**
     * Całkowicie czyści stan logowania i ciasteczka.
     */
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

    /**
     * Prawidłowo weryfikuje czy użytkownik jest zalogowany.
     * Zwraca true wyłącznie po rzeczywistym, zapisanym zalogowaniu (nie dla anonimowego gościa z PHPSESSID).
     */
    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasLoginFlag = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!hasLoginFlag) return false
        val c = prefs.getString(KEY_COOKIES, null)
        return !c.isNullOrBlank()
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
