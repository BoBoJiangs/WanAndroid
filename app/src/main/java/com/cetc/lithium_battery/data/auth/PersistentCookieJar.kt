package com.cetc.lithium_battery.data.auth

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

interface CookieStore {
    fun clear()
}

class PersistentCookieJar(context: Context) : CookieJar, CookieStore {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return

        val now = System.currentTimeMillis()
        val merged = loadAll()
            .filter { it.expiresAt > now }
            .associateBy { it.storageKey() }
            .toMutableMap()

        cookies.forEach { cookie ->
            if (cookie.expiresAt <= now) {
                merged.remove(cookie.storageKey())
            } else {
                merged[cookie.storageKey()] = cookie
            }
        }

        saveAll(merged.values.toList())
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val all = loadAll()
        val valid = all.filter { it.expiresAt > now }
        if (valid.size != all.size) {
            saveAll(valid)
        }
        return valid.filter { it.matches(url) }
    }

    override fun clear() {
        prefs.edit().remove(KEY_COOKIES).apply()
    }

    private fun loadAll(): List<Cookie> {
        val rawCookies = prefs.getStringSet(KEY_COOKIES, emptySet()).orEmpty()
        return rawCookies.mapNotNull { raw ->
            Cookie.parse(BASE_URL, raw)
        }
    }

    private fun saveAll(cookies: List<Cookie>) {
        prefs.edit()
            .putStringSet(KEY_COOKIES, cookies.map { it.toString() }.toSet())
            .apply()
    }

    private fun Cookie.storageKey(): String = "$domain|$path|$name"

    companion object {
        private const val PREFS_NAME = "wan_android_cookies"
        private const val KEY_COOKIES = "cookies"
        private val BASE_URL = HttpUrl.Builder()
            .scheme("https")
            .host("wanandroid.com")
            .build()
    }
}
